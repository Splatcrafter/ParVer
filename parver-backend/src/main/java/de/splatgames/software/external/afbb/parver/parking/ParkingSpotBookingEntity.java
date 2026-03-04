package de.splatgames.software.external.afbb.parver.parking;

import de.splatgames.software.external.afbb.parver.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_spot_bookings")
public class ParkingSpotBookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "release_id", nullable = false)
    private ParkingSpotReleaseEntity release;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booked_by_id", nullable = false)
    private UserEntity bookedBy;

    @Column(name = "booked_from", nullable = false)
    private LocalDateTime bookedFrom;

    @Column(name = "booked_to", nullable = false)
    private LocalDateTime bookedTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ParkingSpotBookingEntity() {
        // JPA
    }

    public ParkingSpotBookingEntity(
            @NotNull final ParkingSpotReleaseEntity release,
            @NotNull final UserEntity bookedBy,
            @NotNull final LocalDateTime bookedFrom,
            @NotNull final LocalDateTime bookedTo) {
        this.release = release;
        this.bookedBy = bookedBy;
        this.bookedFrom = bookedFrom;
        this.bookedTo = bookedTo;
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
    public ParkingSpotReleaseEntity getRelease() {
        return this.release;
    }

    @NotNull
    public UserEntity getBookedBy() {
        return this.bookedBy;
    }

    @NotNull
    public LocalDateTime getBookedFrom() {
        return this.bookedFrom;
    }

    @NotNull
    public LocalDateTime getBookedTo() {
        return this.bookedTo;
    }

    public boolean containsTime(@NotNull final LocalDateTime time) {
        return !time.isBefore(this.bookedFrom) && !time.isAfter(this.bookedTo);
    }

    public boolean overlaps(@NotNull final LocalDateTime from, @NotNull final LocalDateTime to) {
        return from.isBefore(this.bookedTo) && to.isAfter(this.bookedFrom);
    }
}
