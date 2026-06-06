package br.com.tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "sessions",
    indexes = {
        @Index(name = "idx_sessions_client_side_uuid", columnList = "clientSideUuid", unique = true)
    }
)
public class Session {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String clientSideUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private User profile;

    @Column(nullable = false)
    private String type; // "ESTEIRA" ou "RUA"

    @Column(nullable = false)
    private Long durationSeconds;

    @Column(nullable = false)
    private Double distanceKm;

    private Double speedKmh; // Opcional, usado em esteira

    @Column(nullable = false)
    private Double calculatedSpeedKmh;

    @Column(nullable = false)
    private Double paceMinKm;

    @Column(nullable = false)
    private Double burnedCalories;

    @Column(nullable = false)
    private Boolean isStandardCircuit;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Session() {}

    public Session(UUID id, String clientSideUuid, User profile, String type, Long durationSeconds,
                   Double distanceKm, Double speedKmh, Double calculatedSpeedKmh, Double paceMinKm,
                   Double burnedCalories, Boolean isStandardCircuit, LocalDateTime createdAt) {
        this.id = id;
        this.clientSideUuid = clientSideUuid;
        this.profile = profile;
        this.type = type;
        this.durationSeconds = durationSeconds;
        this.distanceKm = distanceKm;
        this.speedKmh = speedKmh;
        this.calculatedSpeedKmh = calculatedSpeedKmh;
        this.paceMinKm = paceMinKm;
        this.burnedCalories = burnedCalories;
        this.isStandardCircuit = isStandardCircuit;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClientSideUuid() {
        return clientSideUuid;
    }

    public void setClientSideUuid(String clientSideUuid) {
        this.clientSideUuid = clientSideUuid;
    }

    public User getProfile() {
        return profile;
    }

    public void setProfile(User profile) {
        this.profile = profile;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(Double speedKmh) {
        this.speedKmh = speedKmh;
    }

    public Double getCalculatedSpeedKmh() {
        return calculatedSpeedKmh;
    }

    public void setCalculatedSpeedKmh(Double calculatedSpeedKmh) {
        this.calculatedSpeedKmh = calculatedSpeedKmh;
    }

    public Double getPaceMinKm() {
        return paceMinKm;
    }

    public void setPaceMinKm(Double paceMinKm) {
        this.paceMinKm = paceMinKm;
    }

    public Double getBurnedCalories() {
        return burnedCalories;
    }

    public void setBurnedCalories(Double burnedCalories) {
        this.burnedCalories = burnedCalories;
    }

    public Boolean getIsStandardCircuit() {
        return isStandardCircuit;
    }

    public void setIsStandardCircuit(Boolean isStandardCircuit) {
        this.isStandardCircuit = isStandardCircuit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
