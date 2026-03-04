package de.splatgames.software.external.afbb.parver.parking;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parking_spot_releases")
public class ParkingSpotReleaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "spot_number", nullable = false)
    private ParkingSpotEntity parkingSpot;

    @Column(name = "available_from", nullable = false)
    private LocalDateTime availableFrom;

    @Column(name = "available_to", nullable = false)
    private LocalDateTime availableTo;

    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParkingSpotBookingEntity> bookings = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ParkingSpotReleaseEntity() {
        // JPA
    }

    public ParkingSpotReleaseEntity(
            @NotNull final ParkingSpotEntity parkingSpot,
            @NotNull final LocalDateTime availableFrom,
            @NotNull final LocalDateTime availableTo) {
        this.parkingSpot = parkingSpot;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
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
    public ParkingSpotEntity getParkingSpot() {
        return this.parkingSpot;
    }

    @NotNull
    public LocalDateTime getAvailableFrom() {
        return this.availableFrom;
    }

    @NotNull
    public LocalDateTime getAvailableTo() {
        return this.availableTo;
    }

    @NotNull
    public List<ParkingSpotBookingEntity> getBookings() {
        return this.bookings;
    }

    public boolean containsTime(@NotNull final LocalDateTime time) {
        return !time.isBefore(this.availableFrom) && !time.isAfter(this.availableTo);
    }

    public boolean overlaps(@NotNull final LocalDateTime from, @NotNull final LocalDateTime to) {
        return from.isBefore(this.availableTo) && to.isAfter(this.availableFrom);
    }
}
