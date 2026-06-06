package br.com.tracker.controller;

import br.com.tracker.dto.UserDTO;
import br.com.tracker.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> upsert(@Valid @RequestBody UserDTO dto) {
        UserDTO result = profileService.upsert(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable UUID id) {
        UserDTO result = profileService.findById(id);
        return ResponseEntity.ok(result);
    }
}
