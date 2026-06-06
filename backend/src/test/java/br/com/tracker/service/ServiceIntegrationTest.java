package br.com.tracker.service;

import br.com.tracker.dto.SessionRequest;
import br.com.tracker.dto.SessionResponse;
import br.com.tracker.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ServiceIntegrationTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ProfileService profileService;

    @Test
    void deveGarantirIdempotenciaAoSalvarMesmaSessaoMultiplasVezes() {
        // 1. Criar e salvar o perfil do usuário
        UUID profileId = UUID.randomUUID();
        UserDTO userDto = new UserDTO(
            profileId,
            "Jean",
            75.0,
            5.0,
            LocalDateTime.now()
        );
        profileService.upsert(userDto);

        // 2. Configurar a requisição de treino
        String clientSideUuid = UUID.randomUUID().toString();
        SessionRequest request = new SessionRequest(
            clientSideUuid,
            profileId,
            "RUA",
            1800L, // 30 min
            5.5,   // 5.5 km
            null,
            75.0   // 75 kg
        );

        // 3. Processar a sessão pela primeira vez
        SessionResponse response1 = sessionService.processSession(request);
        assertNotNull(response1, "A primeira resposta não deve ser nula.");
        assertEquals(clientSideUuid, response1.clientSideUuid());

        // 4. Processar a mesma sessão pela segunda vez
        SessionResponse response2 = sessionService.processSession(request);
        
        // 5. Asserções de Idempotência
        assertNotNull(response2, "A segunda resposta não deve ser nula.");
        assertEquals(response1.id(), response2.id(), "O ID da sessão retornada deve ser o mesmo (Idempotência).");
        assertEquals(response1.createdAt(), response2.createdAt(), "O timestamp de criação deve ser exatamente o mesmo.");
    }
}
