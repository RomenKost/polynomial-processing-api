package com.kostenko.polynomial.processing.api.model;

public interface Term {
    Term multiply(Term term);

    int degree();

    int coefficient();
}
