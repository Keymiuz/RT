package br.com.tracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SessionRequest(
    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    String clientSideUuid,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    UUID profileId,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    String type, // "ESTEIRA" ou "RUA"

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    @Min(value = 1, message = "INVALID_INPUT_PARAMETERS")
    Long durationSeconds,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    @DecimalMin(value = "0.01", message = "INVALID_INPUT_PARAMETERS")
    Double distanceKm,

    Double speedKmh, // Opcional, usado em esteira

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    @DecimalMin(value = "1.0", message = "INVALID_INPUT_PARAMETERS")
    Double weightKg
) {}
