package com.kostenko.polynomial.processing.api.web;

import com.kostenko.polynomial.processing.api.service.processor.PolynomialProcessor;
import com.kostenko.polynomial.processing.api.web.dto.EvaluationResponseDto;
import com.kostenko.polynomial.processing.api.web.dto.PolynomialDto;
import com.kostenko.polynomial.processing.api.web.dto.PolynomialEvaluationDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/polynomials")
public class PolynomialProcessingApiController {
    private final PolynomialProcessor polynomialProcessor;

    @PostMapping("/simplify")
    public ResponseEntity<PolynomialDto> simplifyPolynomial(@RequestBody PolynomialDto request) {
        log.info("Retrieved simplification request ({})", request);
        String simplifiedPolynomialString = polynomialProcessor.simplify(request.polynomial());

        return new ResponseEntity<>(
                new PolynomialDto(simplifiedPolynomialString),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationResponseDto> evaluatePolynomial(@RequestBody PolynomialEvaluationDto request) {
        log.info("Retrieved evaluation request ({})", request);
        int result = polynomialProcessor.evaluate(request.polynomial(), request.x());

        return new ResponseEntity<>(
                new EvaluationResponseDto(result),
                HttpStatus.CREATED
        );
    }
}
