package br.com.tracker.controller;

import br.com.tracker.dto.SessionRequest;
import br.com.tracker.dto.SessionResponse;
import br.com.tracker.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> saveSession(
            @RequestHeader("X-Client-UUID") String xClientUuid,
            @Valid @RequestBody SessionRequest request) {

        if (xClientUuid == null || !xClientUuid.equals(request.clientSideUuid())) {
            throw new IllegalArgumentException("O UUID do cabeçalho X-Client-UUID deve corresponder ao clientSideUuid no corpo da requisição.");
        }

        SessionResponse result = sessionService.processSession(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> listSessionsByProfile(@RequestParam UUID profileId) {
        List<SessionResponse> result = sessionService.getSessionsByProfile(profileId);
        return ResponseEntity.ok(result);
    }
}
