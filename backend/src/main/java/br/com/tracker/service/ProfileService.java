package br.com.tracker.service;

import br.com.tracker.dto.UserDTO;
import br.com.tracker.model.User;
import br.com.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDTO upsert(UserDTO dto) {
        Optional<User> existingUserOpt = userRepository.findById(dto.id());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            // Lógica Last Write Wins (LWW) baseada no updatedAt
            if (dto.updatedAt().isAfter(existingUser.getUpdatedAt())) {
                existingUser.setName(dto.name());
                existingUser.setWeightKg(dto.weightKg());
                existingUser.setTargetPace(dto.targetPace());
                existingUser.setUpdatedAt(dto.updatedAt());
                User saved = userRepository.save(existingUser);
                return toDTO(saved);
            }
            return toDTO(existingUser);
        } else {
            User newUser = new User(
                dto.id(),
                dto.name(),
                dto.weightKg(),
                dto.targetPace(),
                dto.updatedAt()
            );
            User saved = userRepository.save(newUser);
            return toDTO(saved);
        }
    }

    @Transactional(readOnly = true)
    public UserDTO findById(java.util.UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new br.com.tracker.exception.ResourceNotFoundException("Perfil não encontrado com o ID fornecido."));
        return toDTO(user);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getWeightKg(),
            user.getTargetPace(),
            user.getUpdatedAt()
        );
    }
}
