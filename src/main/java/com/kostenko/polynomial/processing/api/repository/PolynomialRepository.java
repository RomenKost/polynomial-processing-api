package com.kostenko.polynomial.processing.api.repository;

import com.kostenko.polynomial.processing.api.repository.entity.PolynomialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolynomialRepository extends JpaRepository<PolynomialEntity, Long> {
    Optional<PolynomialEntity> findByPolynomialRequest(String polynomialRequest);
}
