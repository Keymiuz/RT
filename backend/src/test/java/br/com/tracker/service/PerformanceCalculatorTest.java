package br.com.tracker.service;

import br.com.tracker.dto.CalculationRequest;
import br.com.tracker.dto.CalculationResponse;
import br.com.tracker.exception.InconsistentDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceCalculatorTest {

    private PerformanceCalculator calculator;

    @BeforeEach
    void setUp() {
        this.calculator = new PerformanceCalculatorImpl();
    }

    @Test
    void deveCalcularPaceECaloriasCorretamenteParaCorridaDeRua() {
        // Cenário: Corrida de rua de 5.5km em 30 minutos (1800 segundos) para um corredor de 75kg
        CalculationRequest request = new CalculationRequest(
            "RUA",
            1800L, // 30 minutos
            5.5,   // 5.5 km
            75.0,  // 75 kg
            null   // sem velocidade de máquina
        );

        // Lógica de cálculo matemático conforme a fórmula do PRD:
        // 1. velocidadeEfetivaKmh = 5.5 / 0.5 = 11.0 km/h
        // 2. paceMinKm = 30.0 / 5.5 = 5.4545... min/km (formato decimal)
        // 3. velocidade_m_min = 11.0 * 16.6667 = 183.3337 m/min
        // 4. MET = 3.5 + (0.2 * 183.3337) = 40.16674
        // 5. calorias = (MET * 3.5 * 75.0 / 200.0) * 30.0 = 451.875 kcal
        
        CalculationResponse response = calculator.calculate(request);

        assertNotNull(response);
        assertEquals(5.4545, response.paceMinKm(), 0.001, "O pace em min/km deve estar correto.");
        assertEquals(11.0, response.calculatedSpeedKmh(), 0.01, "A velocidade calculada deve ser de 11 km/h.");
        assertEquals(451.875, response.burnedCalories(), 0.1, "O gasto calórico calculado por MET deve ser ~451.8 kcal.");
        assertTrue(response.isStandardCircuit(), "O circuito de 5.5km deve ser identificado como padrão.");
    }

    @Test
    void deveLancarInconsistentDataExceptionSeDivergenciaVelocidadeEsteiraForSuperiorA15Porcento() {
        // Cenário: Esteira com velocidade informada de 15 km/h,
        // mas tempo de 1800s e distância de 5.5km resultam em velocidade efetiva de 11 km/h.
        // A diferença absoluta (15.0 - 11.0 = 4 km/h) representa ~36.36% de divergência da velocidade efetiva.
        CalculationRequest request = new CalculationRequest(
            "ESTEIRA",
            1800L,
            5.5,
            75.0,
            15.0 // Velocidade informada pela máquina
        );

        assertThrows(InconsistentDataException.class, () -> {
            calculator.calculate(request);
        }, "Deve lançar exceção quando a velocidade informada diverge mais que 15% da velocidade efetiva.");
    }

    @Test
    void deveIdentificarCircuitoPadrao() {
        // Cenário 1: Exatamente 5.5km
        CalculationRequest requestStandard = new CalculationRequest("RUA", 1800L, 5.5, 75.0, null);
        
        // Cenário 2: Distância diferente de 5.5km (ex: 6.0km)
        CalculationRequest requestNonStandard = new CalculationRequest("RUA", 1800L, 6.0, 75.0, null);

        CalculationResponse responseStandard = calculator.calculate(requestStandard);
        CalculationResponse responseNonStandard = calculator.calculate(requestNonStandard);

        assertTrue(responseStandard.isStandardCircuit(), "Distância exata de 5.5km deve marcar isStandardCircuit como true.");
        assertFalse(responseNonStandard.isStandardCircuit(), "Distâncias diferentes de 5.5km devem marcar isStandardCircuit como false.");
    }
}
