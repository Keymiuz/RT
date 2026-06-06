package br.com.tracker.service;

import br.com.tracker.dto.CalculationRequest;
import br.com.tracker.dto.CalculationResponse;
import br.com.tracker.exception.InconsistentDataException;
import org.springframework.stereotype.Service;

@Service
public class PerformanceCalculatorImpl implements PerformanceCalculator {

    @Override
    public CalculationResponse calculate(CalculationRequest request) {
        double distanceKm = request.distanceKm();
        long durationSeconds = request.durationSeconds();
        double weightKg = request.weightKg();

        // 1. Calcular a velocidade efetiva
        double durationHours = durationSeconds / 3600.0;
        double calculatedSpeedKmh = distanceKm / durationHours;

        // 2. Validação para o modo ESTEIRA
        if ("ESTEIRA".equalsIgnoreCase(request.type()) && request.speedKmh() != null) {
            double speedKmh = request.speedKmh();
            double differencePercent = Math.abs(speedKmh - calculatedSpeedKmh) / calculatedSpeedKmh;

            if (differencePercent > 0.15) {
                throw new InconsistentDataException(
                    String.format("Divergência de velocidade na esteira superior a 15%%. Informada: %.2f km/h, Efetiva: %.2f km/h",
                        speedKmh, calculatedSpeedKmh)
                );
            }
        }

        // 3. Calcular o pace médio (min/km)
        double durationMinutes = durationSeconds / 60.0;
        double paceMinKm = durationMinutes / distanceKm;

        // 4. Calcular o gasto calórico usando a Opção A (MET realístico baseado no ACSM)
        double burnedCalories = calculateCalories(calculatedSpeedKmh, weightKg, durationMinutes);

        // 5. Verificar se é o circuito padrão de 5.5km
        boolean isStandardCircuit = Double.compare(distanceKm, 5.5) == 0;

        return new CalculationResponse(
            paceMinKm,
            calculatedSpeedKmh,
            burnedCalories,
            isStandardCircuit
        );
    }

    private double calculateCalories(double speedKmh, double weightKg, double durationMinutes) {
        // velocidade em metros por minuto
        double velocidade_m_min = speedKmh * 16.6667;
        
        // VO2 = 3.5 + (0.2 * velocidade_m_min)
        double vo2 = 3.5 + (0.2 * velocidade_m_min);
        
        // MET = VO2 / 3.5
        double met = vo2 / 3.5;
        
        // Calorias = (MET * 3.5 * pesoKg / 200.0) * tempo_minutos
        // Simplifica matematicamente para: (vo2 * pesoKg / 200.0) * tempo_minutos
        return (met * 3.5 * weightKg / 200.0) * durationMinutes;
    }
}
