package com.kostenko.polynomial.processing.api.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvaluationResponseDto(@JsonProperty("result") int result) { }
