package br.com.tracker.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(
    UUID id,
    String clientSideUuid,
    UUID profileId,
    String type,
    Long durationSeconds,
    Double distanceKm,
    Double speedKmh,
    Double calculatedSpeedKmh,
    Double paceMinKm,
    Double burnedCalories,
    Boolean isStandardCircuit,
    LocalDateTime createdAt
) {}
