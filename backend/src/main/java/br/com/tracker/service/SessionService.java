package br.com.tracker.service;

import br.com.tracker.dto.CalculationRequest;
import br.com.tracker.dto.CalculationResponse;
import br.com.tracker.dto.SessionRequest;
import br.com.tracker.dto.SessionResponse;
import br.com.tracker.model.Session;
import br.com.tracker.model.User;
import br.com.tracker.repository.SessionRepository;
import br.com.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PerformanceCalculator performanceCalculator;

    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          PerformanceCalculator performanceCalculator) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.performanceCalculator = performanceCalculator;
    }

    @Transactional
    public SessionResponse processSession(SessionRequest request) {
        // 1. Validar idempotência usando o clientSideUuid
        Optional<Session> existingSessionOpt = sessionRepository.findByClientSideUuid(request.clientSideUuid());
        if (existingSessionOpt.isPresent()) {
            return toResponseDTO(existingSessionOpt.get());
        }

        // 2. Buscar o perfil
        User profile = userRepository.findById(request.profileId())
            .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado com o ID fornecido."));

        // 3. Executar o cálculo matemático de pace, calorias e validações de esteira no motor do Java
        CalculationRequest calcRequest = new CalculationRequest(
            request.type(),
            request.durationSeconds(),
            request.distanceKm(),
            request.weightKg(),
            request.speedKmh()
        );
        
        // Isso dispara a InconsistentDataException caso a diferença seja > 15% na esteira
        CalculationResponse calcResponse = performanceCalculator.calculate(calcRequest);

        // 4. Salvar o novo treino
        Session newSession = new Session(
            UUID.randomUUID(),
            request.clientSideUuid(),
            profile,
            request.type(),
            request.durationSeconds(),
            request.distanceKm(),
            request.speedKmh(),
            calcResponse.calculatedSpeedKmh(),
            calcResponse.paceMinKm(),
            calcResponse.burnedCalories(),
            calcResponse.isStandardCircuit(),
            LocalDateTime.now()
        );

        Session savedSession = sessionRepository.save(newSession);
        return toResponseDTO(savedSession);
    }

    @Transactional(readOnly = true)
    public java.util.List<SessionResponse> getSessionsByProfile(UUID profileId) {
        if (!userRepository.existsById(profileId)) {
            throw new br.com.tracker.exception.ResourceNotFoundException("Perfil não encontrado com o ID fornecido.");
        }
        return sessionRepository.findByProfileId(profileId).stream()
            .map(this::toResponseDTO)
            .toList();
    }

    private SessionResponse toResponseDTO(Session session) {
        return new SessionResponse(
            session.getId(),
            session.getClientSideUuid(),
            session.getProfile().getId(),
            session.getType(),
            session.getDurationSeconds(),
            session.getDistanceKm(),
            session.getSpeedKmh(),
            session.getCalculatedSpeedKmh(),
            session.getPaceMinKm(),
            session.getBurnedCalories(),
            session.getIsStandardCircuit(),
            session.getCreatedAt()
        );
    }
}
