package com.kostenko.polynomial.processing.api.model.impl;

import com.kostenko.polynomial.processing.api.model.Term;

public record TermImpl(int degree, int coefficient) implements Term {
    @Override
    public Term multiply(Term term) {
        return new TermImpl(degree + term.degree(), coefficient * term.coefficient());
    }
}
