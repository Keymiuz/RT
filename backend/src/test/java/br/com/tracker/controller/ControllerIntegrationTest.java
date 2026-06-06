package br.com.tracker.controller;

import br.com.tracker.config.WebMvcConfig;
import br.com.tracker.dto.SessionRequest;
import br.com.tracker.dto.SessionResponse;
import br.com.tracker.dto.UserDTO;
import br.com.tracker.exception.GlobalExceptionHandler;
import br.com.tracker.interceptor.ClientSideUUIDInterceptor;
import br.com.tracker.service.ProfileService;
import br.com.tracker.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProfileController.class, SessionController.class})
@Import({WebMvcConfig.class, ClientSideUUIDInterceptor.class, GlobalExceptionHandler.class})
public class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private SessionService sessionService;

    @Test
    void deveBloquearCriacaoDeSessaoSemHeaderXClientUUID() throws Exception {
        UUID profileId = UUID.randomUUID();
        String clientSideUuid = UUID.randomUUID().toString();
        SessionRequest request = new SessionRequest(
            clientSideUuid,
            profileId,
            "RUA",
            1800L,
            5.5,
            null,
            75.0
        );

        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_PARAMETERS"))
                .andExpect(jsonPath("$.message").value("O cabeçalho X-Client-UUID é obrigatório para esta operação."));
    }

    @Test
    void devePermitirCriacaoDeSessaoComHeaderXClientUUIDValido() throws Exception {
        UUID profileId = UUID.randomUUID();
        String clientSideUuid = UUID.randomUUID().toString();
        SessionRequest request = new SessionRequest(
            clientSideUuid,
            profileId,
            "RUA",
            1800L,
            5.5,
            null,
            75.0
        );

        SessionResponse response = new SessionResponse(
            UUID.randomUUID(),
            clientSideUuid,
            profileId,
            "RUA",
            1800L,
            5.5,
            null,
            11.0,
            5.45,
            451.87,
            true,
            LocalDateTime.now()
        );

        when(sessionService.processSession(any(SessionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                .header("X-Client-UUID", clientSideUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSideUuid").value(clientSideUuid))
                .andExpect(jsonPath("$.burnedCalories").value(451.87));
    }

    @Test
    void deveRetornarBadRequestSeHeaderNaoBaterComBody() throws Exception {
        UUID profileId = UUID.randomUUID();
        String clientSideUuidBody = UUID.randomUUID().toString();
        String clientSideUuidHeader = UUID.randomUUID().toString(); // UUID diferente do body
        SessionRequest request = new SessionRequest(
            clientSideUuidBody,
            profileId,
            "RUA",
            1800L,
            5.5,
            null,
            75.0
        );

        mockMvc.perform(post("/api/sessions")
                .header("X-Client-UUID", clientSideUuidHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_PARAMETERS"))
                .andExpect(jsonPath("$.message").value("O UUID do cabeçalho X-Client-UUID deve corresponder ao clientSideUuid no corpo da requisição."));
    }

    @Test
    void deveCriarPerfilCorretamente() throws Exception {
        UUID profileId = UUID.randomUUID();
        UserDTO userDto = new UserDTO(
            profileId,
            "Jean",
            75.0,
            5.0,
            LocalDateTime.now()
        );

        when(profileService.upsert(any(UserDTO.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId.toString()))
                .andExpect(jsonPath("$.name").value("Jean"));
    }

    @Test
    void deveListarSessoesPorPerfil() throws Exception {
        UUID profileId = UUID.randomUUID();

        when(sessionService.getSessionsByProfile(profileId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sessions")
                .param("profileId", profileId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
