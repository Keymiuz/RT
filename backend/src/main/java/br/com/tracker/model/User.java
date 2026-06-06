package br.com.tracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    private String name;

    private Double weightKg;

    private Double targetPace;

    private LocalDateTime updatedAt;

    public User() {}

    public User(UUID id, String name, Double weightKg, Double targetPace, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.weightKg = weightKg;
        this.targetPace = targetPace;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getTargetPace() {
        return targetPace;
    }

    public void setTargetPace(Double targetPace) {
        this.targetPace = targetPace;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
