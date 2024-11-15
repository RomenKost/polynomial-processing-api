package com.kostenko.polynomial.processing.api.service.processor.impl;

import com.kostenko.polynomial.processing.api.exception.ErrorCode;
import com.kostenko.polynomial.processing.api.exception.PolynomialBaseException;
import com.kostenko.polynomial.processing.api.model.Polynomial;
import com.kostenko.polynomial.processing.api.repository.EvaluationRepository;
import com.kostenko.polynomial.processing.api.repository.PolynomialRepository;
import com.kostenko.polynomial.processing.api.repository.entity.EvaluationEntity;
import com.kostenko.polynomial.processing.api.repository.entity.PolynomialEntity;
import com.kostenko.polynomial.processing.api.service.mapper.PolynomialMapper;
import com.kostenko.polynomial.processing.api.service.parser.PolynomialParser;
import com.kostenko.polynomial.processing.api.service.processor.PolynomialProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class PolynomialProcessorImpl implements PolynomialProcessor {
    private final PolynomialParser polynomialParser;
    private final PolynomialMapper polynomialMapper;

    private final PolynomialRepository polynomialRepository;
    private final EvaluationRepository evaluationRepository;

    @Override
    @Retryable(maxAttempts = 2, retryFor = PSQLException.class)
    public String simplify(String polynomial) {
        log.info("Simplification process has been started for polynomial ({}). Trying to find cached values.", polynomial);
        Optional<PolynomialEntity> polynomialEntity = polynomialRepository.findByPolynomialRequest(polynomial);

        polynomialEntity.map(PolynomialEntity::getErrorCode)
                .ifPresent(this::rethrowException);

        Optional<String> simplifiedPolynomialStringOptional = polynomialEntity.map(PolynomialEntity::getSimplifiedPolynomial);
        simplifiedPolynomialStringOptional.ifPresent(simplifiedPolynomialString ->
                log.info("Found cached value for polynomial ({}): ({})", polynomial, simplifiedPolynomialString)
        );

        return simplifiedPolynomialStringOptional.orElseGet(() -> processSimplification(polynomial));

    }

    @Override
    @Retryable(retryFor = PSQLException.class)
    public int evaluate(String polynomial, String x) {
        log.info("Evaluation process has been started for polynomial ({}) and x={}. Trying to find cached result.", polynomial, x);

        Optional<Integer> evaluationResultOptional = evaluationRepository.findByRequestPolynomialAndX(polynomial, x)
                .map(EvaluationEntity::getEvaluationResult);
        evaluationResultOptional.ifPresent(evaluationResult ->
                log.info("Found cached value for polynomial ({}) and x={}: result={}", polynomial, x, evaluationResult)
        );

        return evaluationResultOptional.orElseGet(() -> processEvaluationWithoutCachedX(polynomial, x));
    }

    private String processSimplification(String polynomial) {
        log.info("Cached entity for polynomial ({}) wasn't found. Starting parsing and simplification process.", polynomial);
        Polynomial parsedPolynomial = polynomialParser.parsePolynomial(polynomial);
        Polynomial simplifiedPolynomial = parsedPolynomial.simplify();

        String simplifiedPolynomialString = polynomialMapper.mapPolynomialToString(simplifiedPolynomial);
        polynomialRepository.save(new PolynomialEntity(polynomial, simplifiedPolynomialString));
        log.info("Simplification process was completed. Saved polynomial ({}) to cache", polynomial);
        return simplifiedPolynomialString;
    }

    private int processEvaluationWithoutCachedX(String polynomialString, String x) {
        log.info("Cached entity for polynomial ({}) and x={} wasn't found. Trying to find cached polynomial.", polynomialString, x);

        EvaluationEntity evaluationEntity = new EvaluationEntity();
        evaluationEntity.setEvaluationRequest(x);

        Optional<PolynomialEntity> polynomialEntity = polynomialRepository.findByPolynomialRequest(polynomialString);
        polynomialEntity.ifPresent(evaluationEntity::setPolynomialEntity);
        polynomialEntity.map(PolynomialEntity::getErrorCode)
                .ifPresent(this::rethrowException);

        int parsedX = polynomialParser.parseX(x);

        Optional<String> simplifiedPolynomialOptional = polynomialEntity.map(PolynomialEntity::getSimplifiedPolynomial);
        simplifiedPolynomialOptional.ifPresent(simplifiedPolynomialString ->
                log.info("Found cached simplified polynomial for polynomial ({}): ({})", polynomialString, simplifiedPolynomialString)
        );

        Optional<Integer> optionalResult = simplifiedPolynomialOptional.map(polynomialParser::parsePolynomial)
                .map(polynomial -> polynomial.evaluate(parsedX));

        optionalResult.ifPresent(evaluationEntity::setEvaluationResult);
        optionalResult.ifPresent(result -> evaluationRepository.save(evaluationEntity));
        optionalResult.ifPresent(result ->
                log.info("Evaluation process was completed. Saved x={} and result={} to cache", x, result)
        );
        return optionalResult.orElseGet(() -> processEvaluation(polynomialString, x, parsedX));
    }

    private int processEvaluation(String polynomialString, String x, int parsedX) {
        log.info("Cached entity for polynomial ({}) wasn't found. Trying to parse, simplify and evaluate data.", polynomialString);
        Polynomial parsedPolynomial = polynomialParser.parsePolynomial(polynomialString);
        Polynomial simplifiedPolynomial = parsedPolynomial.simplify();

        String simplifiedPolynomialString = polynomialMapper.mapPolynomialToString(simplifiedPolynomial);
        int result = simplifiedPolynomial.evaluate(parsedX);

        PolynomialEntity polynomialEntity = new PolynomialEntity(polynomialString, simplifiedPolynomialString);
        EvaluationEntity evaluationEntity = new EvaluationEntity(polynomialEntity, x, result);
        evaluationRepository.save(evaluationEntity);
        log.info("Evaluation process was completed. Saved polynomial ({}), x={} and result={} to cache", polynomialString, x, result);
        return result;
    }

    private void rethrowException(String errorCode) {
        log.info("Rethrowing exception {} from cache", errorCode);
        String[] errorCodeArray = errorCode.split(" ");
        int errorCodeInt = Integer.parseInt(errorCodeArray[0]);
        ErrorCode errorCodeEnum = ErrorCode.getErrorCodeByCode(errorCodeInt);

        Object[] parameters = null;
        if (errorCodeArray.length > 1) {
            parameters = Arrays.stream(errorCodeArray).skip(1).toArray();
        }

        throw new PolynomialBaseException(errorCodeEnum, parameters);
    }
}
