package com.kostenko.polynomial.processing.api.service.processor.impl;

import com.kostenko.polynomial.processing.api.exception.ErrorCode;
import com.kostenko.polynomial.processing.api.exception.PolynomialBaseException;
import com.kostenko.polynomial.processing.api.model.Polynomial;
import com.kostenko.polynomial.processing.api.model.impl.PolynomialImpl;
import com.kostenko.polynomial.processing.api.model.impl.TermImpl;
import com.kostenko.polynomial.processing.api.repository.EvaluationRepository;
import com.kostenko.polynomial.processing.api.repository.PolynomialRepository;
import com.kostenko.polynomial.processing.api.repository.entity.EvaluationEntity;
import com.kostenko.polynomial.processing.api.repository.entity.PolynomialEntity;
import com.kostenko.polynomial.processing.api.service.mapper.PolynomialMapper;
import com.kostenko.polynomial.processing.api.service.parser.PolynomialParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolynomialProcessorImplTest {
    @Mock
    private PolynomialParser polynomialParser;
    @Mock
    private PolynomialMapper polynomialMapper;

    @Mock
    private PolynomialRepository polynomialRepository;
    @Mock
    private EvaluationRepository evaluationRepository;

    @InjectMocks
    private PolynomialProcessorImpl polynomialProcessor;

    @Test
    void testSimplify() {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        Polynomial polynomial = new PolynomialImpl(List.of(
                new TermImpl(2, 2),
                new TermImpl(1, 3),
                new TermImpl(0, -5),
                new TermImpl(2, 1),
                new TermImpl(1, 1)
        ));
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";
        Polynomial simplifiedPolynomial = new PolynomialImpl(List.of(
                new TermImpl(2, 3),
                new TermImpl(1, 4),
                new TermImpl(0, -5)
        ));

        when(polynomialRepository.findByPolynomialRequest(polynomialString))
                .thenReturn(Optional.empty());
        when(polynomialParser.parsePolynomial(polynomialString))
                .thenReturn(polynomial);
        when(polynomialMapper.mapPolynomialToString(simplifiedPolynomial))
                .thenReturn(simplifiedPolynomialString);

        String actual = polynomialProcessor.simplify(polynomialString);

        verify(polynomialRepository, times(1))
                .save(new PolynomialEntity(polynomialString, simplifiedPolynomialString));

        assertEquals(simplifiedPolynomialString, actual);
    }

    @Test
    void testSimplifyCachedCorrectPolynomial() {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";

        when(polynomialRepository.findByPolynomialRequest(polynomialString))
                .thenReturn(Optional.of(new PolynomialEntity(polynomialString, simplifiedPolynomialString)));

        String actual = polynomialProcessor.simplify(polynomialString);
        verify(polynomialParser, times(0))
                .parsePolynomial(any());
        verify(polynomialMapper, times(0))
                .mapPolynomialToString(any());
        assertEquals(simplifiedPolynomialString, actual);
    }

    @Test
    void testSimplifyCachedIncorrectPolynomialShouldThrowPolynomialBaseException() {
        String polynomialString = "x^2*2";
        String term = "+x^2*2";
        String errorCode = "5 +x^2*2";

        PolynomialEntity polynomialEntity = new PolynomialEntity();
        polynomialEntity.setPolynomialRequest(polynomialString);
        polynomialEntity.setErrorCode(errorCode);

        PolynomialBaseException expected = new PolynomialBaseException(ErrorCode.IMPOSSIBLE_TO_PARSE_DEGREE, term);

        when(polynomialRepository.findByPolynomialRequest(polynomialString))
                .thenReturn(Optional.of(polynomialEntity));

        PolynomialBaseException actual = assertThrows(
                PolynomialBaseException.class,
                () -> polynomialProcessor.simplify(polynomialString)
        );

        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(5, actual.getErrorCode());
        assertArrayEquals(new Object[]{term}, actual.getParameters());
    }

    @Test
    void testEvaluation() {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        Polynomial polynomial = new PolynomialImpl(List.of(
                new TermImpl(2, 2),
                new TermImpl(1, 3),
                new TermImpl(0, -5),
                new TermImpl(2, 1),
                new TermImpl(1, 1)
        ));
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";
        Polynomial simplifiedPolynomial = new PolynomialImpl(List.of(
                new TermImpl(2, 3),
                new TermImpl(1, 4),
                new TermImpl(0, -5)
        ));
        String x = "2";
        int parsedX = 2;
        int expected = 15;

        PolynomialEntity polynomialEntity = new PolynomialEntity(polynomialString, simplifiedPolynomialString);
        EvaluationEntity evaluationEntity = new EvaluationEntity(polynomialEntity, x, expected);

        when(evaluationRepository.findByRequestPolynomialAndX(polynomialString, x))
                .thenReturn(Optional.empty());
        when(polynomialParser.parseX(x))
                .thenReturn(parsedX);
        when(polynomialRepository.findByPolynomialRequest(polynomialString))
                .thenReturn(Optional.empty());
        when(polynomialParser.parsePolynomial(polynomialString))
                .thenReturn(polynomial);
        when(polynomialMapper.mapPolynomialToString(simplifiedPolynomial))
                .thenReturn(simplifiedPolynomialString);

        int actual = polynomialProcessor.evaluate(polynomialString, x);

        verify(evaluationRepository, times(1))
                .save(evaluationEntity);

        assertEquals(expected, actual);
    }

    @Test
    void testEvaluateCachedCorrectPolynomialAndX() {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        String x = "2";
        int expected = 15;

        when(evaluationRepository.findByRequestPolynomialAndX(polynomialString, x))
                .thenReturn(Optional.of(new EvaluationEntity(null, x, expected)));

        int actual = polynomialProcessor.evaluate(polynomialString, x);

        verify(polynomialParser, times(0))
                .parseX(any());
        verify(polynomialParser, times(0))
                .parsePolynomial(any());
        verify(polynomialMapper, times(0))
                .mapPolynomialToString(any());

        assertEquals(expected, actual);
    }

    @Test
    void testEvaluateCachedCorrectPolynomial() {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";

        Polynomial simplifiedPolynomial = new PolynomialImpl(List.of(
                new TermImpl(2, 3),
                new TermImpl(1, 4),
                new TermImpl(0, -5)
        ));

        PolynomialEntity polynomialEntity = new PolynomialEntity(polynomialString, simplifiedPolynomialString);

        String x = "2";
        int parsedX = 2;
        int expected = 15;

        when(evaluationRepository.findByRequestPolynomialAndX(polynomialString, x))
                .thenReturn(Optional.empty());
        when(polynomialParser.parseX(x))
                .thenReturn(parsedX);
        when(polynomialParser.parsePolynomial(simplifiedPolynomialString))
                .thenReturn(simplifiedPolynomial);
        when(polynomialRepository.findByPolynomialRequest(polynomialString))
                .thenReturn(Optional.of(polynomialEntity));

        int actual = polynomialProcessor.evaluate(polynomialString, x);

        verify(polynomialMapper, times(0))
                .mapPolynomialToString(any());
        verify(evaluationRepository, times(1))
                .save(new EvaluationEntity(polynomialEntity, x, expected));

        assertEquals(expected, actual);
    }

    @Test
    void testEvaluateCachedIncorrectPolynomialShouldThrowPolynomialBaseException() {
        String polynomialString = "2.0 * x";
        String errorCode = "1";

        String x = "2";

        PolynomialEntity polynomialEntity = new PolynomialEntity();
        polynomialEntity.setPolynomialRequest(polynomialString);
        polynomialEntity.setErrorCode(errorCode);

        PolynomialBaseException expected = new PolynomialBaseException(ErrorCode.UNSUPPORTED_POLYNOMIAL_TYPE);

        when(evaluationRepository.findByRequestPolynomialAndX(polynomialString, x))
                .thenReturn(Optional.empty());
        when(polynomialRepository.findByPolynomialRequest(polynomialString))
                .thenReturn(Optional.of(polynomialEntity));

        PolynomialBaseException actual = assertThrows(
                PolynomialBaseException.class,
                () -> polynomialProcessor.evaluate(polynomialString, x)
        );

        verify(polynomialParser, times(0))
                .parseX(any());
        verify(polynomialParser, times(0))
                .parsePolynomial(any());
        verify(polynomialMapper, times(0))
                .mapPolynomialToString(any());

        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(1, actual.getErrorCode());
        assertNull(actual.getParameters());
    }
}
