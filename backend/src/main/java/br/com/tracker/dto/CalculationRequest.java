package br.com.tracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CalculationRequest(
    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    String type, // "ESTEIRA" ou "RUA"

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    @Min(value = 1, message = "INVALID_INPUT_PARAMETERS")
    Long durationSeconds,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    @DecimalMin(value = "0.01", message = "INVALID_INPUT_PARAMETERS")
    Double distanceKm,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    @DecimalMin(value = "1.0", message = "INVALID_INPUT_PARAMETERS")
    Double weightKg,

    Double speedKmh // Opcional, necessário se for ESTEIRA
) {}
