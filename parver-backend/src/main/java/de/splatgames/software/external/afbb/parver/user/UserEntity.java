package de.splatgames.software.external.afbb.parver.user;

import de.splatgames.software.external.afbb.parver.parking.ParkingSpotEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @OneToOne(mappedBy = "owner")
    private ParkingSpotEntity parkingSpot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() {
        // JPA
    }

    public UserEntity(
            @NotNull final String username,
            @NotNull final String displayName,
            @NotNull final String passwordHash,
            @NotNull final UserRole role) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @NotNull
    public Long getId() {
        return this.id;
    }

    @NotNull
    public String getUsername() {
        return this.username;
    }

    public void setUsername(@NotNull final String username) {
        this.username = username;
    }

    @NotNull
    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(@NotNull final String displayName) {
        this.displayName = displayName;
    }

    @NotNull
    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setPasswordHash(@NotNull final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @NotNull
    public UserRole getRole() {
        return this.role;
    }

    public void setRole(@NotNull final UserRole role) {
        this.role = role;
    }

    @Nullable
    public ParkingSpotEntity getParkingSpot() {
        return this.parkingSpot;
    }

    public boolean hasParkingSpot() {
        return this.parkingSpot != null;
    }

    @NotNull
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    @NotNull
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }
}
