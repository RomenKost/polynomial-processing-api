package com.kostenko.polynomial.processing.api.model.impl;

import com.kostenko.polynomial.processing.api.model.Polynomial;
import com.kostenko.polynomial.processing.api.model.Term;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public record PolynomialImpl(List<Term> terms) implements Polynomial {
    public PolynomialImpl(List<Term> terms) {
        this.terms = terms.stream()
                .filter(term -> term.coefficient() != 0)
                .toList();
    }

    @Override
    public Polynomial simplify() {
        Map<Integer, Integer> termsMap = new TreeMap<>(Comparator.reverseOrder());

        for (Term term : terms) {
            termsMap.merge(term.degree(), term.coefficient(), Integer::sum);
        }

        return mapToPolynomial(termsMap);
    }

    @Override
    public int evaluate(int x) {
        return terms.stream()
                .mapToInt(term -> term.coefficient() * (int) Math.pow(x, term.degree()))
                .sum();
    }

    @Override
    public Polynomial multiply(Polynomial polynomial) {
        Map<Integer, Integer> termsMap = new TreeMap<>(Comparator.reverseOrder());

        for (Term term1 : terms) {
            for (Term term2 : polynomial) {
                Term multipliedTerms = term1.multiply(term2);
                termsMap.merge(multipliedTerms.degree(), multipliedTerms.coefficient(), Integer::sum);
            }
        }

        return mapToPolynomial(termsMap);
    }

    @NonNull
    @Override
    public Iterator<Term> iterator() {
        return terms.iterator();
    }

    private Polynomial mapToPolynomial(Map<Integer, Integer> termsMap) {
        List<Term> resultTerms = termsMap.entrySet()
                .stream()
                .map(entry -> new TermImpl(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
        return new PolynomialImpl(resultTerms);
    }
}
