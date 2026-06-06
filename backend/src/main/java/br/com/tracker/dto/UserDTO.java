package br.com.tracker.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UserDTO(
    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    UUID id,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    String name,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    @DecimalMin(value = "1.0", message = "INVALID_INPUT_PARAMETERS")
    Double weightKg,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    @DecimalMin(value = "1.0", message = "INVALID_INPUT_PARAMETERS")
    Double targetPace,

    @NotNull(message = "INVALID_INPUT_PARAMETERS")
    LocalDateTime updatedAt
) {}
