package com.kostenko.polynomial.processing.api.exception;

import lombok.Getter;

@Getter
public class PolynomialBaseException extends RuntimeException {
    private final int errorCode;
    private Object[] parameters;

    public PolynomialBaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public PolynomialBaseException(ErrorCode errorCode, Object... parameters) {
        super(errorCode.getMessage().formatted(parameters));
        this.errorCode = errorCode.getCode();
        this.parameters = parameters;
    }
}
