package br.com.tracker.controller;

import br.com.tracker.dto.CalculationRequest;
import br.com.tracker.dto.CalculationResponse;
import br.com.tracker.exception.GlobalExceptionHandler;
import br.com.tracker.exception.InconsistentDataException;
import br.com.tracker.service.PerformanceCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerformanceController.class)
@Import(GlobalExceptionHandler.class)
public class PerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PerformanceCalculator calculator;

    @Test
    void deveRetornarOkQuandoRequisicaoForValida() throws Exception {
        CalculationRequest request = new CalculationRequest("RUA", 1800L, 5.5, 75.0, null);
        CalculationResponse response = new CalculationResponse(5.45, 11.0, 451.87, true);

        when(calculator.calculate(any(CalculationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/performance/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paceMinKm").value(5.45))
                .andExpect(jsonPath("$.calculatedSpeedKmh").value(11.0))
                .andExpect(jsonPath("$.burnedCalories").value(451.87))
                .andExpect(jsonPath("$.isStandardCircuit").value(true));
    }

    @Test
    void deveRetornarBadRequestQuandoRequisicaoTiverValoresInvalidos() throws Exception {
        // Cenário: durationSeconds = 0 (deve falhar no @Min(1))
        CalculationRequest request = new CalculationRequest("RUA", 0L, 5.5, 75.0, null);

        mockMvc.perform(post("/api/performance/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_PARAMETERS"))
                .andExpect(jsonPath("$.message").value("Parâmetros de entrada inválidos ou ausentes na requisição."));
    }

    @Test
    void deveRetornarUnprocessableEntityQuandoOcorreInconsistenciaDeEsteira() throws Exception {
        CalculationRequest request = new CalculationRequest("ESTEIRA", 1800L, 5.5, 75.0, 15.0);

        when(calculator.calculate(any(CalculationRequest.class)))
                .thenThrow(new InconsistentDataException("Divergência de velocidade na esteira superior a 15%."));

        mockMvc.perform(post("/api/performance/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("INCONSISTENT_TREADMILL_DATA"))
                .andExpect(jsonPath("$.message").value("Divergência de velocidade na esteira superior a 15%."));
    }
}
