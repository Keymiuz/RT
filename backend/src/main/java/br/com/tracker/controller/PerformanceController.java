package br.com.tracker.controller;

import br.com.tracker.dto.CalculationRequest;
import br.com.tracker.dto.CalculationResponse;
import br.com.tracker.service.PerformanceCalculator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceCalculator calculator;

    public PerformanceController(PerformanceCalculator calculator) {
        this.calculator = calculator;
    }

    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponse> calculate(@Valid @RequestBody CalculationRequest request) {
        CalculationResponse response = calculator.calculate(request);
        return ResponseEntity.ok(response);
    }
}
