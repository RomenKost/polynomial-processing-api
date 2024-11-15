package com.kostenko.polynomial.processing.api.exception.parser.impl;

import com.kostenko.polynomial.processing.api.exception.ErrorCode;
import com.kostenko.polynomial.processing.api.exception.parser.PolynomialParserException;

public class PolynomialParserXRelatedException extends PolynomialParserException {
    public PolynomialParserXRelatedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
