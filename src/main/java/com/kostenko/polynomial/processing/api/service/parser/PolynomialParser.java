package com.kostenko.polynomial.processing.api.service.parser;

import com.kostenko.polynomial.processing.api.model.Polynomial;

public interface PolynomialParser {
    Polynomial parsePolynomial(String polynomialString);

    int parseX(String x);
}
