package com.kostenko.polynomial.processing.api.service.parser.impl;

import com.kostenko.polynomial.processing.api.exception.parser.impl.PolynomialParserPolynomialRelatedException;
import com.kostenko.polynomial.processing.api.exception.parser.impl.PolynomialParserXRelatedException;
import com.kostenko.polynomial.processing.api.model.Polynomial;
import com.kostenko.polynomial.processing.api.model.Term;
import com.kostenko.polynomial.processing.api.model.impl.PolynomialImpl;
import com.kostenko.polynomial.processing.api.model.impl.TermImpl;
import com.kostenko.polynomial.processing.api.service.parser.PolynomialParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kostenko.polynomial.processing.api.exception.ErrorCode.*;

@Slf4j
@Service
public class PolynomialParserImpl implements PolynomialParser {
    private final Pattern termPattern = Pattern.compile("([+-][^+-]*)");

    private static final String SINGLE_POLYNOMIAL_REGEX = "^[0-9x\\-+*^]+$";
    private static final String COMPLEX_POLYNOMIAL_REGEX = "^\\([0-9x\\-+*^]*\\)(\\*\\([0-9x\\-+*^]*\\))*$";

    private static final String X_REGEX = "^-?[0-9]{1,9}";

    private static final String COMPLEX_POLYNOMIAL_DELIMITER_REGEX = "\\)\\*\\(";
    private static final String POLYNOMIAL_TERM_COEFFICIENT_REGEX = "^[+-][1-9][0-9]{0,8}$";
    private static final String POLYNOMIAL_TERM_DEGREE_REGEX = "^[0-9]{0,9}$";

    @Override
    public Polynomial parsePolynomial(String polynomialString) {
        log.info("Parsing process has been started for polynomial: ({})", polynomialString);

        String polynomialStringWithoutWhitespaces = polynomialString.replace(" ", "");

        try {
            if (polynomialStringWithoutWhitespaces.matches(SINGLE_POLYNOMIAL_REGEX)) {
                return parseSimplePolynomial(polynomialStringWithoutWhitespaces);
            }

            if (polynomialStringWithoutWhitespaces.matches(COMPLEX_POLYNOMIAL_REGEX)) {
                return parseComplexPolynomial(polynomialStringWithoutWhitespaces);
            }

            log.error(UNSUPPORTED_POLYNOMIAL_TYPE.getMessage());
            throw new PolynomialParserPolynomialRelatedException(UNSUPPORTED_POLYNOMIAL_TYPE);
        } catch (PolynomialParserPolynomialRelatedException e) {
            e.setPolynomialString(polynomialString);
            throw e;
        }
    }

    @Override
    public int parseX(String x) {
        log.info("Parsing process has been started for x = {}", x);
        if (x.matches(X_REGEX)) {
            int result = Integer.parseInt(x);
            log.info("Parsing process has been completed for x = {}: result = {}", x, result);
            return result;
        }

        log.error(UNSUPPORTED_X_TYPE.getMessage());
        throw new PolynomialParserXRelatedException(UNSUPPORTED_X_TYPE);
    }

    private Polynomial parseSimplePolynomial(String polynomialString) {
        log.info("Parsing process has been started for simple polynomial: ({})", polynomialString);

        String polynomialStringWithSilentFirstSign = addSilentFirstSign(polynomialString);
        List<Term> terms = parseTerms(polynomialStringWithSilentFirstSign);

        log.info("Parsing process has been completed for simple polynomial: ({})", polynomialString);
        return new PolynomialImpl(terms);
    }

    private Polynomial parseComplexPolynomial(String polynomialString) {
        log.info("Parsing process has been started for complex polynomial: ({})", polynomialString);

        String polynomialStringWithoutBrackets = polynomialString.substring(1, polynomialString.length() - 1);
        String[] simplePolynomialStrings = polynomialStringWithoutBrackets.split(COMPLEX_POLYNOMIAL_DELIMITER_REGEX);

        Polynomial result = Arrays.stream(simplePolynomialStrings)
                .map(this::parseSimplePolynomial)
                .reduce(new PolynomialImpl(List.of(new TermImpl(0, 1))), Polynomial::multiply);

        log.info("Parsing process has been completed for complex polynomial: ({})", polynomialString);
        return result;
    }

    private String addSilentFirstSign(String polynomialString) {
        log.debug("Adding silent first sign for polynomial ({})", polynomialString);
        return polynomialString.charAt(0) != '-'
                ? "+" + polynomialString
                : polynomialString;
    }

    private List<Term> parseTerms(String polynomialString) {
        log.debug("Parsing terms for polynomial ({})", polynomialString);
        Matcher matcher = termPattern.matcher(polynomialString);

        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results.stream()
                .map(this::parseTerm)
                .toList();
    }

    private Term parseTerm(String term) {
        log.debug("Parsing polynomial term ({})", term);
        int indexOfX = term.indexOf('x');

        if (indexOfX == -1) {
            return new TermImpl(0, parseCoefficient(term));
        }

        if (indexOfX == 1) {
            int coefficient = term.charAt(0) == '-' ? -1 : 1;
            if (term.length() == indexOfX + 1) {
                return new TermImpl(1, coefficient);
            }
            int degree = parseDegree(term, indexOfX);
            return new TermImpl(degree, coefficient);
        }

        if (indexOfX == term.length() - 1) {
            int coefficient = parseCoefficient(term, indexOfX );
            return new TermImpl(1, coefficient);
        }

        int degree = parseDegree(term, indexOfX);
        int coefficient = parseCoefficient(term, indexOfX);
        return new TermImpl(degree, coefficient);
    }

    private int parseCoefficient(String coefficient) {
        log.debug("Parsing polynomial term coefficient ({})", coefficient);
        if (coefficient.matches(POLYNOMIAL_TERM_COEFFICIENT_REGEX)) {
            return Integer.parseInt(coefficient);
        }
        log.error(IMPOSSIBLE_TO_PARSE_COEFFICIENT.getMessage(), coefficient);
        throw new PolynomialParserPolynomialRelatedException(IMPOSSIBLE_TO_PARSE_COEFFICIENT, coefficient);
    }

    private int parseCoefficient(String term, int indexOfX) {
        log.debug("Parsing polynomial coefficient for term ({})", term);
        if (term.charAt(indexOfX - 1) != '*') {
            log.error(INCORRECT_MULTIPLY_SIGN_POSITION.getMessage(), term);
            throw new PolynomialParserPolynomialRelatedException(INCORRECT_MULTIPLY_SIGN_POSITION, term);
        }
        String coefficient = term.substring(0, indexOfX - 1);
        return parseCoefficient(coefficient);
    }

    private int parseDegree(String term, int indexOfX) {
        log.debug("Parsing polynomial degree for term ({})", term);
        if (term.charAt(indexOfX + 1) != '^') {
            log.error(INCORRECT_DEGREE_SIGN_POSITION.getMessage(), term);
            throw new PolynomialParserPolynomialRelatedException(INCORRECT_DEGREE_SIGN_POSITION, term);
        }
        String degree = term.substring(indexOfX + 2);
        if (degree.matches(POLYNOMIAL_TERM_DEGREE_REGEX)) {
            return Integer.parseInt(degree);
        }
        log.error(IMPOSSIBLE_TO_PARSE_DEGREE.getMessage(), term);
        throw new PolynomialParserPolynomialRelatedException(IMPOSSIBLE_TO_PARSE_DEGREE, term);
    }
}
