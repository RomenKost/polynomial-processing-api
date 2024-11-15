package com.kostenko.polynomial.processing.api.exception.handler;

import com.kostenko.polynomial.processing.api.exception.ErrorCode;
import com.kostenko.polynomial.processing.api.exception.PolynomialBaseException;
import com.kostenko.polynomial.processing.api.exception.parser.impl.PolynomialParserPolynomialRelatedException;
import com.kostenko.polynomial.processing.api.repository.PolynomialRepository;
import com.kostenko.polynomial.processing.api.repository.entity.PolynomialEntity;
import com.kostenko.polynomial.processing.api.web.PolynomialProcessingApiController;
import com.kostenko.polynomial.processing.api.web.dto.ErrorResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@ControllerAdvice(assignableTypes = PolynomialProcessingApiController.class)
public class PolynomialWebExceptionHandler {
    private final PolynomialRepository polynomialRepository;

    @ExceptionHandler(PolynomialBaseException.class)
    public ResponseEntity<ErrorResponseDto> handlePolynomialBaseException(PolynomialBaseException webException) {
        log.error("Retrieved exception PolynomialBaseException", webException);
        return new ResponseEntity<>(
                new ErrorResponseDto(webException.getErrorCode(), webException.getMessage()),
                HttpStatusCode.valueOf(400)
        );
    }

    @ExceptionHandler(PolynomialParserPolynomialRelatedException.class)
    public ResponseEntity<ErrorResponseDto> handlePolynomialRelatedException(PolynomialParserPolynomialRelatedException polynomialRelatedException) {
        log.error("Retrieved polynomial related exception", polynomialRelatedException);

        String polynomialSting = polynomialRelatedException.getPolynomialString();
        int errorCode = polynomialRelatedException.getErrorCode();
        Object[] parameters = polynomialRelatedException.getParameters();

        if (polynomialSting != null) {
            savePolynomialExceptionToCache(polynomialSting, errorCode, parameters);
        }

        return new ResponseEntity<>(
                new ErrorResponseDto(polynomialRelatedException.getErrorCode(), polynomialRelatedException.getMessage()),
                HttpStatusCode.valueOf(400)
        );
    }

    private void savePolynomialExceptionToCache(String polynomialString, int code, Object[] parameters) {
        log.info("Saving polynomial exception to cache for ({}), code={}", polynomialString, code);
        String errorCodeString = "" + code;
        if (parameters != null) {
            errorCodeString += " " + Arrays.stream(parameters)
                    .map(Object::toString)
                    .collect(Collectors.joining(" "));
        }

        PolynomialEntity polynomialEntity = new PolynomialEntity();
        polynomialEntity.setPolynomialRequest(polynomialString);
        polynomialEntity.setErrorCode(errorCodeString);

        try {
            polynomialRepository.save(polynomialEntity);
        } catch (DataAccessException e) {
            log.error("Impossible to save {}", polynomialEntity);
        }
    }
}
