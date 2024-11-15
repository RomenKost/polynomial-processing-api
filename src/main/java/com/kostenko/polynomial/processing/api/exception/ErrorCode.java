package com.kostenko.polynomial.processing.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNSUPPORTED_POLYNOMIAL_TYPE(1, "Unsupported polynomial type. The example of a correct polynomial: '(x^2 - 5) * (2*x + 2)', '3*x^3 - 7*x + 1'"),
    UNSUPPORTED_X_TYPE(2, "Unsupported x type. The example of a correct x: '12', '-5'"),

    IMPOSSIBLE_TO_PARSE_COEFFICIENT(3, "Impossible to parse coefficient (%s). The example of a correct coefficient: '12', '-5'"),
    INCORRECT_MULTIPLY_SIGN_POSITION(4, "Impossible to parse term (%s): incorrect sign '*' position: it should be before 'x' in each term. The example of a correct terms: '2 * x^2', '-5 * x'"),

    IMPOSSIBLE_TO_PARSE_DEGREE(5, "Impossible to parse degree from term (%s). The example of a correct terms: '2 * x^2', 'x ^ 5'"),
    INCORRECT_DEGREE_SIGN_POSITION(6, "Impossible to parse term (%s): incorrect sign '^' position: it should be after 'x' in each term. The example of a correct terms: '2 * x^2', 'x ^ 5'"),

    UNKNOWN_EXCEPTION(999, "Unknown exception");

    private final int code;
    private final String message;

    public static ErrorCode getErrorCodeByCode(int code) {
        return Arrays.stream(ErrorCode.values())
                .filter(errorCode -> errorCode.getCode() == code)
                .findAny()
                .orElse(UNKNOWN_EXCEPTION);
    }
}
