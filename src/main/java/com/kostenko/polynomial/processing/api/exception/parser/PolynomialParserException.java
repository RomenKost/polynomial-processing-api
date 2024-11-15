package com.kostenko.polynomial.processing.api.exception.parser;

import com.kostenko.polynomial.processing.api.exception.ErrorCode;
import com.kostenko.polynomial.processing.api.exception.PolynomialBaseException;

public class PolynomialParserException extends PolynomialBaseException {
    public PolynomialParserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PolynomialParserException(ErrorCode errorCode, Object... parameters) {
        super(errorCode, parameters);
    }
}
