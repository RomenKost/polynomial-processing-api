package com.kostenko.polynomial.processing.api.service.processor;

public interface PolynomialProcessor {
    String simplify(String polynomial);

    int evaluate(String polynomial, String x);
}
