package br.com.tracker.service;

import br.com.tracker.dto.CalculationRequest;
import br.com.tracker.dto.CalculationResponse;

public interface PerformanceCalculator {
    CalculationResponse calculate(CalculationRequest request);
}
