package com.kostenko.polynomial.processing.api.service.mapper.impl;

import com.kostenko.polynomial.processing.api.service.mapper.PolynomialMapper;
import com.kostenko.polynomial.processing.api.model.Polynomial;
import com.kostenko.polynomial.processing.api.model.Term;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PolynomialMapperImpl implements PolynomialMapper {
    @Override
    public String mapPolynomialToString(Polynomial polynomial) {
        log.info("Polynomial to string mapping process was started for ({})", polynomial);
        StringBuilder result = new StringBuilder();
        polynomial.forEach(term -> mapTermToString(result, term));
        reduceFirstSignFromResult(result);
        log.info("Polynomial to string mapping process was completed for ({})", polynomial);
        return result.toString();
    }

    private void mapTermToString(StringBuilder result, Term term) {
        log.debug("Processing term to string for term ({})", term);
        mapCoefficientSignToString(result, term);
        mapCoefficientToString(result, term);
        mapDegreeToString(result, term);
    }

    private void reduceFirstSignFromResult(StringBuilder result) {
        log.debug("Reducing first sign");
        if (result.charAt(1) == '+') {
            result.delete(0, 3);
        } else {
            result.deleteCharAt(2);
            result.deleteCharAt(0);
        }
    }

    private void mapCoefficientSignToString(StringBuilder result, Term term) {
        log.debug("Processing term to coefficient sign for term ({})", term);
        if (term.coefficient() > 0) {
            result.append(" + ");
        } else {
            result.append(" - ");
        }
    }

    private void mapCoefficientToString(StringBuilder result, Term term) {
        log.debug("Processing term to coefficient for term ({})", term);
        if (term.coefficient() != 1 && term.coefficient() != -1) {
            result.append(Math.abs(term.coefficient()));
            if (term.degree() != 0) {
                result.append("*");
            }
        } else {
            if (term.degree() == 0) {
                result.append(Math.abs(term.coefficient()));
            }
        }
    }

    private void mapDegreeToString(StringBuilder result, Term term) {
        log.debug("Processing term to degree for term ({})", term);
        if (term.degree() != 0) {
            result.append("x");
            if (term.degree() != 1) {
                result.append("^").append(term.degree());
            }
        }
    }
}
