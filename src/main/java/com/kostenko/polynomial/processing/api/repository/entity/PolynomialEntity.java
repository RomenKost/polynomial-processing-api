package com.kostenko.polynomial.processing.api.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "polynomials")
public class PolynomialEntity {
    @Id
    @Column(name = "polynomial_id")
    @SequenceGenerator(sequenceName = "polynomial_seq", name = "polynomial_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "polynomial_seq")
    private Long id;

    @Column(name = "polynomial_request")
    private String polynomialRequest;

    @Column(name = "simplified_polynomial")
    private String simplifiedPolynomial;

    @Column(name = "error_code")
    private String errorCode;

    public PolynomialEntity(String polynomialRequest, String simplifiedPolynomial) {
        this.polynomialRequest = polynomialRequest;
        this.simplifiedPolynomial = simplifiedPolynomial;
    }
}
