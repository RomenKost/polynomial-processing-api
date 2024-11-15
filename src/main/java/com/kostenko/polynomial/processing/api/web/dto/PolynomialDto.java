package com.kostenko.polynomial.processing.api.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PolynomialDto(@JsonProperty(value = "polynomial", required = true) String polynomial) { }
