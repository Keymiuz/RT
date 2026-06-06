package br.com.tracker.dto;

public record ErrorResponse(
    String errorCode,
    String message
) {}
