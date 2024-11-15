package com.kostenko.polynomial.processing.api.service.parser.impl;

import com.kostenko.polynomial.processing.api.exception.ErrorCode;
import com.kostenko.polynomial.processing.api.exception.parser.impl.PolynomialParserPolynomialRelatedException;
import com.kostenko.polynomial.processing.api.exception.parser.impl.PolynomialParserXRelatedException;
import com.kostenko.polynomial.processing.api.model.Polynomial;
import com.kostenko.polynomial.processing.api.model.impl.PolynomialImpl;
import com.kostenko.polynomial.processing.api.model.impl.TermImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.kostenko.polynomial.processing.api.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

class PolynomialParserImplTest {
    private final PolynomialParserImpl polynomialParser = new PolynomialParserImpl();

    @ParameterizedTest
    @MethodSource("polynomialProvider")
    void parsePolynomialTest(String polynomialString, Polynomial expectedPolynomial) {
        Polynomial actualPolynomial = polynomialParser.parsePolynomial(polynomialString);

        assertEquals(expectedPolynomial, actualPolynomial);
    }

    @Test
    void parseXTest() {
        int expected = -36;
        int actual = polynomialParser.parseX("-36");

        assertEquals(expected, actual);
    }

    @Test
    void parseIncorrectXShouldThrowPolynomialParserXRelatedException() {
        PolynomialParserPolynomialRelatedException expected = new PolynomialParserPolynomialRelatedException(ErrorCode.UNSUPPORTED_X_TYPE);
        String x = "abc";

        PolynomialParserXRelatedException actual = assertThrows(
                PolynomialParserXRelatedException.class,
                () -> polynomialParser.parseX(x)
        );

        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(ErrorCode.UNSUPPORTED_X_TYPE.getCode(), actual.getErrorCode());
        assertNull(actual.getParameters());
    }

    @Test
    void parseIncorrectPolynomialShouldThrowPolynomialParserPolynomialRelatedException() {
        PolynomialParserPolynomialRelatedException expected = new PolynomialParserPolynomialRelatedException(ErrorCode.UNSUPPORTED_POLYNOMIAL_TYPE);
        String polynomialString = "abc";

        PolynomialParserPolynomialRelatedException actual = assertThrows(
                PolynomialParserPolynomialRelatedException.class,
                () -> polynomialParser.parsePolynomial(polynomialString)
        );

        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(polynomialString, actual.getPolynomialString());
        assertEquals(ErrorCode.UNSUPPORTED_POLYNOMIAL_TYPE.getCode(), actual.getErrorCode());
        assertNull(actual.getParameters());
    }

    @ParameterizedTest
    @MethodSource("polynomialWithIncorrectTerm")
    void parsePolynomialWithIncorrectTermShouldThrowPolynomialParserPolynomialRelatedException(
            String polynomialString, String term, ErrorCode errorCode
    ) {
        PolynomialParserPolynomialRelatedException expected = new PolynomialParserPolynomialRelatedException(errorCode, term);

        PolynomialParserPolynomialRelatedException actual = assertThrows(
                PolynomialParserPolynomialRelatedException.class,
                () -> polynomialParser.parsePolynomial(polynomialString)
        );

        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(polynomialString, actual.getPolynomialString());
        assertEquals(errorCode.getCode(), actual.getErrorCode());
        assertArrayEquals(new Object[]{term}, actual.getParameters());
    }

    private static Stream<Arguments> polynomialWithIncorrectTerm() {
        return Stream.of(
                Arguments.of("2x", "+2x", INCORRECT_MULTIPLY_SIGN_POSITION),
                Arguments.of("x2", "+x2", INCORRECT_DEGREE_SIGN_POSITION),
                Arguments.of("5*5", "+5*5", IMPOSSIBLE_TO_PARSE_COEFFICIENT),
                Arguments.of("x^x", "+x^x", IMPOSSIBLE_TO_PARSE_DEGREE)
        );
    }

    private static Stream<Arguments> polynomialProvider() {
        return Stream.of(
                Arguments.of("2*x^2 + 3*x^2 -5", new PolynomialImpl(List.of(
                        new TermImpl(2, 2),
                        new TermImpl(2, 3),
                        new TermImpl(0, -5)
                ))),
                Arguments.of("2*x^2+3*x-5+x^2+x", new PolynomialImpl(List.of(
                        new TermImpl(2, 2),
                        new TermImpl(1, 3),
                        new TermImpl(0, -5),
                        new TermImpl(2, 1),
                        new TermImpl(1, 1)
                ))),
                Arguments.of("-x", new PolynomialImpl(List.of(
                        new TermImpl(1, -1)
                ))),
                Arguments.of("(x +2)*(x- 1)", new PolynomialImpl(List.of(
                        new TermImpl(2, 1),
                        new TermImpl(1, 1),
                        new TermImpl(0, -2)
                )))
        );
    }
}
