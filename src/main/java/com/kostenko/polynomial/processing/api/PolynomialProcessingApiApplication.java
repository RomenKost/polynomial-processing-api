package com.kostenko.polynomial.processing.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class PolynomialProcessingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PolynomialProcessingApiApplication.class, args);
    }
}
