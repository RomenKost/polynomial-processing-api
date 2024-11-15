package com.kostenko.polynomial.processing.api.model;

public interface Polynomial extends Iterable<Term> {
    Polynomial simplify();

    int evaluate(int x);

    Polynomial multiply(Polynomial polynomial);
}
