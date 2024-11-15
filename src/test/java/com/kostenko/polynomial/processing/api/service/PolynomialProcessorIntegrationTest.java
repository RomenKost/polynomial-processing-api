package com.kostenko.polynomial.processing.api.service;

import com.kostenko.polynomial.processing.api.repository.EvaluationRepository;
import com.kostenko.polynomial.processing.api.repository.PolynomialRepository;
import com.kostenko.polynomial.processing.api.repository.entity.EvaluationEntity;
import com.kostenko.polynomial.processing.api.repository.entity.PolynomialEntity;
import com.kostenko.polynomial.processing.api.service.parser.PolynomialParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class PolynomialProcessorIntegrationTest {
    @Container
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:14.7-alpine")
            .withDatabaseName("test_database")
            .withUsername("username")
            .withPassword("password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolynomialRepository polynomialRepository;
    @Autowired
    private EvaluationRepository evaluationRepository;

    @SpyBean
    private PolynomialParser polynomialParser;

    @AfterEach
    void clear() {
        evaluationRepository.deleteAll();
        polynomialRepository.deleteAll();
    }

    @Test
    void testPolynomialSimplification() throws Exception {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";

        MockHttpServletResponse response = sendSimplifyRequest(polynomialString);

        List<PolynomialEntity> expectedPolynomialEntities = List.of(new PolynomialEntity(polynomialString, simplifiedPolynomialString));
        List<PolynomialEntity> actualPolynomialEntities = polynomialRepository.findAll();

        List<EvaluationEntity> actualEvaluationEntities = evaluationRepository.findAll();

        String expected = "{\"polynomial\":\"%s\"}".formatted(simplifiedPolynomialString);
        String actual = response.getContentAsString();

        assertEquals(expected, actual);
        assertEquals(CREATED.value(), response.getStatus());
        assertPolynomialEntitiesEquals(expectedPolynomialEntities, actualPolynomialEntities);
        assertTrue(actualEvaluationEntities.isEmpty());
    }

    @Test
    void testPolynomialEvaluation() throws Exception {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";

        String x = "2";
        int result = 15;

        MockHttpServletResponse response = sendEvaluateRequest(polynomialString, x);

        List<PolynomialEntity> expectedPolynomialEntities = List.of(new PolynomialEntity(polynomialString, simplifiedPolynomialString));
        List<PolynomialEntity> actualPolynomialEntities = polynomialRepository.findAll();
        List<EvaluationEntity> expectedEvaluationEntities = List.of(new EvaluationEntity(expectedPolynomialEntities.get(0), x, result));
        List<EvaluationEntity> actualEvaluationEntities = evaluationRepository.findAll();

        String expected = "{\"result\":%d}".formatted(result);
        String actual = response.getContentAsString();

        assertEquals(expected, actual);
        assertEquals(CREATED.value(), response.getStatus());
        assertPolynomialEntitiesEquals(expectedPolynomialEntities, actualPolynomialEntities);
        assertEvaluationEntitiesEquals(expectedEvaluationEntities, actualEvaluationEntities);
    }

    @Test
    void testPolynomialSimplificationWithCachedPolynomial() throws Exception {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";

        PolynomialEntity cachedPolynomialEntity = new PolynomialEntity(polynomialString, simplifiedPolynomialString);
        polynomialRepository.save(cachedPolynomialEntity);

        MockHttpServletResponse response = sendSimplifyRequest(polynomialString);


        List<PolynomialEntity> expectedPolynomialEntities = List.of(cachedPolynomialEntity);
        List<PolynomialEntity> actualPolynomialEntities = polynomialRepository.findAll();
        List<EvaluationEntity> actualEvaluationEntities = evaluationRepository.findAll();

        String expected = "{\"polynomial\":\"%s\"}".formatted(simplifiedPolynomialString);
        String actual = response.getContentAsString();

        verify(polynomialParser, times(0))
                .parsePolynomial(any());

        assertEquals(expected, actual);
        assertEquals(CREATED.value(), response.getStatus());
        assertPolynomialEntitiesEquals(expectedPolynomialEntities, actualPolynomialEntities);
        assertTrue(actualEvaluationEntities.isEmpty());
    }

    @Test
    void testPolynomialEvaluationWithCachedPolynomialAndX() throws Exception {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";

        String x = "2";
        int result = 15;

        PolynomialEntity cachedPolynomialEntity = new PolynomialEntity(polynomialString, simplifiedPolynomialString);
        EvaluationEntity cachedEvaluationEntity = new EvaluationEntity(cachedPolynomialEntity, x, result);
        evaluationRepository.save(cachedEvaluationEntity);

        MockHttpServletResponse response = sendEvaluateRequest(polynomialString, x);

        List<PolynomialEntity> expectedPolynomialEntities = List.of(new PolynomialEntity(polynomialString, simplifiedPolynomialString));
        List<PolynomialEntity> actualPolynomialEntities = polynomialRepository.findAll();
        List<EvaluationEntity> expectedEvaluationEntities = List.of(new EvaluationEntity(expectedPolynomialEntities.get(0), x, result));
        List<EvaluationEntity> actualEvaluationEntities = evaluationRepository.findAll();

        String expected = "{\"result\":%d}".formatted(result);
        String actual = response.getContentAsString();

        verify(polynomialParser, times(0))
                .parsePolynomial(any());
        verify(polynomialParser, times(0))
                .parseX(any());

        assertEquals(expected, actual);
        assertEquals(CREATED.value(), response.getStatus());
        assertPolynomialEntitiesEquals(expectedPolynomialEntities, actualPolynomialEntities);
        assertEvaluationEntitiesEquals(expectedEvaluationEntities, actualEvaluationEntities);
    }

    @Test
    void testPolynomialEvaluationWithCachedPolynomial() throws Exception {
        String polynomialString = "2*x^2 + 3*x - 5 + x^2 + x";
        String simplifiedPolynomialString = "3*x^2 + 4*x - 5";

        String x = "2";
        int result = 15;

        PolynomialEntity cachedPolynomialEntity = new PolynomialEntity(polynomialString, simplifiedPolynomialString);
        polynomialRepository.save(cachedPolynomialEntity);

        MockHttpServletResponse response = sendEvaluateRequest(polynomialString, x);

        List<PolynomialEntity> expectedPolynomialEntities = List.of(new PolynomialEntity(polynomialString, simplifiedPolynomialString));
        List<PolynomialEntity> actualPolynomialEntities = polynomialRepository.findAll();
        List<EvaluationEntity> expectedEvaluationEntities = List.of(new EvaluationEntity(expectedPolynomialEntities.get(0), x, result));
        List<EvaluationEntity> actualEvaluationEntities = evaluationRepository.findAll();

        String expected = "{\"result\":%d}".formatted(result);
        String actual = response.getContentAsString();

        verify(polynomialParser)
                .parsePolynomial(simplifiedPolynomialString);

        assertEquals(expected, actual);
        assertEquals(CREATED.value(), response.getStatus());
        assertPolynomialEntitiesEquals(expectedPolynomialEntities, actualPolynomialEntities);
        assertEvaluationEntitiesEquals(expectedEvaluationEntities, actualEvaluationEntities);
    }


    @Test
    void testPolynomialSimplificationWhenPolynomialIsIncorrectReturnErrorDto() throws Exception {
        String polynomialString = "2*2";

        MockHttpServletResponse response = sendSimplifyRequest(polynomialString);

        PolynomialEntity polynomialEntity = new PolynomialEntity();
        polynomialEntity.setPolynomialRequest(polynomialString);
        polynomialEntity.setErrorCode("3 +2*2");

        List<PolynomialEntity> expectedPolynomialEntities = List.of(polynomialEntity);
        List<PolynomialEntity> actualPolynomialEntities = polynomialRepository.findAll();

        List<EvaluationEntity> actualEvaluationEntities = evaluationRepository.findAll();

        String expected = "{\"code\":3,\"message\":\"Impossible to parse coefficient (+%s). The example of a correct coefficient: '12', '-5'\"}".formatted(polynomialString);
        String actual = response.getContentAsString();

        assertEquals(expected, actual);
        assertEquals(BAD_REQUEST.value(), response.getStatus());
        assertPolynomialEntitiesEquals(expectedPolynomialEntities, actualPolynomialEntities);
        assertTrue(actualEvaluationEntities.isEmpty());
    }

    @Test
    void testPolynomialEvaluationWhenXIsIncorrectReturnErrorDto() throws Exception {
        String polynomialString = "x";
        String x = "abc";

        MockHttpServletResponse response = sendEvaluateRequest(polynomialString, x);

        List<PolynomialEntity> actualPolynomialEntities = polynomialRepository.findAll();
        List<EvaluationEntity> actualEvaluationEntities = evaluationRepository.findAll();

        String expected = "{\"code\":2,\"message\":\"Unsupported x type. The example of a correct x: '12', '-5'\"}";
        String actual = response.getContentAsString();

        assertEquals(expected, actual);
        assertEquals(BAD_REQUEST.value(), response.getStatus());
        assertTrue(actualPolynomialEntities.isEmpty());
        assertTrue(actualEvaluationEntities.isEmpty());
    }

    private MockHttpServletResponse sendSimplifyRequest(String polynomialString) throws Exception {
        return mockMvc.perform(post("/api/polynomials/simplify")
                        .content("{\"polynomial\": \"%s\"}".formatted(polynomialString))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse sendEvaluateRequest(String polynomialString, String x) throws Exception {
        return mockMvc.perform(post("/api/polynomials/evaluate")
                        .content("{\"polynomial\": \"%s\", \"x\": \"%s\"}".formatted(polynomialString, x))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    private void assertPolynomialEntitiesEquals(List<PolynomialEntity> expected, List<PolynomialEntity> actual) {
        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            PolynomialEntity expectedItem = expected.get(0);
            PolynomialEntity actualItem = actual.get(0);

            assertEquals(expectedItem.getPolynomialRequest(), actualItem.getPolynomialRequest());
            assertEquals(expectedItem.getSimplifiedPolynomial(), actualItem.getSimplifiedPolynomial());
            assertEquals(expectedItem.getErrorCode(), actualItem.getErrorCode());
        }
    }

    private void assertEvaluationEntitiesEquals(List<EvaluationEntity> expected, List<EvaluationEntity> actual) {
        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            EvaluationEntity expectedItem = expected.get(0);
            EvaluationEntity actualItem = actual.get(0);

            assertPolynomialEntitiesEquals(
                    List.of(expectedItem.getPolynomialEntity()),
                    List.of(actualItem.getPolynomialEntity())
            );
            assertEquals(expectedItem.getEvaluationRequest(), actualItem.getEvaluationRequest());
            assertEquals(expectedItem.getEvaluationResult(), actualItem.getEvaluationResult());
        }
    }
}
