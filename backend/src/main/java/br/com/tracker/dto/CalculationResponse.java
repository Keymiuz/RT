package br.com.tracker.dto;

public record CalculationResponse(
    Double paceMinKm,
    Double calculatedSpeedKmh,
    Double burnedCalories,
    Boolean isStandardCircuit
) {}
