package com.kostenko.polynomial.processing.api.service.mapper.impl;

import com.kostenko.polynomial.processing.api.model.Polynomial;
import com.kostenko.polynomial.processing.api.model.impl.PolynomialImpl;
import com.kostenko.polynomial.processing.api.model.impl.TermImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolynomialMapperImplTest {
    private final PolynomialMapperImpl polynomialMapper = new PolynomialMapperImpl();

    @ParameterizedTest
    @MethodSource("polynomialProvider")
    void parsePolynomialTest(Polynomial polynomial, String expected) {
        String actual = polynomialMapper.mapPolynomialToString(polynomial);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> polynomialProvider() {
        return Stream.of(
                Arguments.of(new PolynomialImpl(List.of(
                        new TermImpl(2, 2),
                        new TermImpl(1, 3),
                        new TermImpl(0, -5)
                )), "2*x^2 + 3*x - 5"),
                Arguments.of(new PolynomialImpl(List.of(
                        new TermImpl(2, -1),
                        new TermImpl(1, 2),
                        new TermImpl(0, -1)
                )), "-x^2 + 2*x - 1"),
                Arguments.of(new PolynomialImpl(List.of(
                        new TermImpl(3, 1)
                )), "x^3")
        );
    }
}
