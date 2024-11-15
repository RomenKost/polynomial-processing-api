package com.kostenko.polynomial.processing.api.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "evaluations")
public class EvaluationEntity {
    @Id
    @Column(name = "evaluation_id")
    @SequenceGenerator(sequenceName = "evaluation_seq", name = "evaluation_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evaluation_seq")
    private Long id;

    @JoinColumn(name = "polynomial_id", referencedColumnName = "polynomial_id")
    @ManyToOne(targetEntity = PolynomialEntity.class, cascade = CascadeType.PERSIST)
    private PolynomialEntity polynomialEntity;

    @Column(name = "evaluation_request")
    private String evaluationRequest;

    @Column(name = "evaluation_result")
    private Integer evaluationResult;

    public EvaluationEntity(PolynomialEntity polynomialEntity, String evaluationRequest, Integer evaluationResult) {
        this.polynomialEntity = polynomialEntity;
        this.evaluationRequest = evaluationRequest;
        this.evaluationResult = evaluationResult;
    }
}
