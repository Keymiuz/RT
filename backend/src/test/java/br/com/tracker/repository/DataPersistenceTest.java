package br.com.tracker.repository;

import br.com.tracker.model.Session;
import br.com.tracker.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class DataPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void deveSalvarSessionEBuscarPorClientSideUuid() {
        // 1. Criar e salvar o usuário (perfil obrigatório)
        User user = new User(
            UUID.randomUUID(),
            "Jean",
            75.0,
            5.0,
            LocalDateTime.now()
        );
        userRepository.save(user);

        // 2. Criar a sessão vinculada ao usuário
        UUID sessionId = UUID.randomUUID();
        String clientSideUuid = UUID.randomUUID().toString();
        Session session = new Session(
            sessionId,
            clientSideUuid,
            user,
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

        // 3. Salvar a sessão
        Session savedSession = sessionRepository.save(session);
        assertNotNull(savedSession);
        assertEquals(sessionId, savedSession.getId());

        // 4. Buscar pelo clientSideUuid e validar
        Optional<Session> retrievedSessionOpt = sessionRepository.findByClientSideUuid(clientSideUuid);
        assertTrue(retrievedSessionOpt.isPresent());

        Session retrievedSession = retrievedSessionOpt.get();
        assertEquals(sessionId, retrievedSession.getId());
        assertEquals(clientSideUuid, retrievedSession.getClientSideUuid());
        assertEquals("RUA", retrievedSession.getType());
        assertEquals(user.getId(), retrievedSession.getProfile().getId());
    }

    @Test
    void deveImpedirDuplicidadeDeClientSideUuidNoBanco() {
        // 1. Criar e salvar o usuário (perfil)
        User user = new User(
            UUID.randomUUID(),
            "Jean",
            75.0,
            5.0,
            LocalDateTime.now()
        );
        userRepository.save(user);

        String sharedClientSideUuid = UUID.randomUUID().toString();

        // 2. Criar e salvar a primeira sessão
        Session session1 = new Session(
            UUID.randomUUID(),
            sharedClientSideUuid,
            user,
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
        sessionRepository.save(session1);

        // 3. Criar a segunda sessão com o mesmo clientSideUuid
        Session session2 = new Session(
            UUID.randomUUID(),
            sharedClientSideUuid,
            user,
            "ESTEIRA",
            1200L,
            3.0,
            10.0,
            9.0,
            6.66,
            250.0,
            false,
            LocalDateTime.now()
        );

        // 4. Deve lançar DataIntegrityViolationException devido à restrição de coluna unique + index
        assertThrows(DataIntegrityViolationException.class, () -> {
            sessionRepository.saveAndFlush(session2);
        }, "Deve explodir exceção de integridade por duplicidade de clientSideUuid.");
    }
}
