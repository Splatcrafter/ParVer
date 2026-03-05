package de.splatgames.software.external.afbb.parver.parking;

import de.splatgames.software.external.afbb.parver.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Entity
@Table(name = "parking_spots")
public class ParkingSpotEntity {

    @Id
    @Column(name = "spot_number", nullable = false)
    private Integer spotNumber;

    @Column(name = "area", nullable = false)
    private String area;

    @OneToOne
    @JoinColumn(name = "owner_id", unique = true)
    private UserEntity owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ParkingSpotEntity() {
        // JPA
    }

    public ParkingSpotEntity(@NotNull final Integer spotNumber, @NotNull final String area) {
        this.spotNumber = spotNumber;
        this.area = area;
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
    public Integer getSpotNumber() {
        return this.spotNumber;
    }

    @NotNull
    public String getArea() {
        return this.area;
    }

    @Nullable
    public UserEntity getOwner() {
        return this.owner;
    }

    public void setOwner(@Nullable final UserEntity owner) {
        this.owner = owner;
    }

    public boolean isOccupied() {
        return this.owner != null;
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
