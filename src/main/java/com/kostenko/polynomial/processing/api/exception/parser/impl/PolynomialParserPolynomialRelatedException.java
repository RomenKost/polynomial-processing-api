package com.kostenko.polynomial.processing.api.exception.parser.impl;

import com.kostenko.polynomial.processing.api.exception.ErrorCode;
import com.kostenko.polynomial.processing.api.exception.parser.PolynomialParserException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolynomialParserPolynomialRelatedException extends PolynomialParserException {
    private String polynomialString;

    public PolynomialParserPolynomialRelatedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PolynomialParserPolynomialRelatedException(ErrorCode errorCode, Object... parameters) {
        super(errorCode, parameters);
    }
}
