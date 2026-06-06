package br.com.tracker.repository;

import br.com.tracker.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByClientSideUuid(String clientSideUuid);
    java.util.List<Session> findByProfileId(UUID profileId);
}
