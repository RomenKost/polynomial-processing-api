package com.kostenko.polynomial.processing.api.repository;

import com.kostenko.polynomial.processing.api.repository.entity.EvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<EvaluationEntity, Long> {
    @Query("SELECT e FROM EvaluationEntity e " +
            "JOIN e.polynomialEntity p " +
            "WHERE p.polynomialRequest = :polynomial AND e.evaluationRequest = :x")
    Optional<EvaluationEntity> findByRequestPolynomialAndX(@Param("polynomial") String polynomial,
                                                           @Param("x") String x);
}
