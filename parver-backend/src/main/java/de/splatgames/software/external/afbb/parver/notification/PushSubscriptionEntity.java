package de.splatgames.software.external.afbb.parver.notification;

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

@Entity
@Table(name = "push_subscriptions")
public class PushSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 512)
    private String endpoint;

    @Column(nullable = false, length = 256)
    private String p256dh;

    @Column(nullable = false, length = 64)
    private String auth;

    @Column(name = "seeking_parking", nullable = false)
    private boolean seekingParking;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PushSubscriptionEntity() {
        // JPA
    }

    public PushSubscriptionEntity(
            @NotNull final UserEntity user,
            @NotNull final String endpoint,
            @NotNull final String p256dh,
            @NotNull final String auth,
            final boolean seekingParking) {
        this.user = user;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.seekingParking = seekingParking;
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
    public UserEntity getUser() {
        return this.user;
    }

    @NotNull
    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(@NotNull final String endpoint) {
        this.endpoint = endpoint;
    }

    @NotNull
    public String getP256dh() {
        return this.p256dh;
    }

    public void setP256dh(@NotNull final String p256dh) {
        this.p256dh = p256dh;
    }

    @NotNull
    public String getAuth() {
        return this.auth;
    }

    public void setAuth(@NotNull final String auth) {
        this.auth = auth;
    }

    public boolean isSeekingParking() {
        return this.seekingParking;
    }

    public void setSeekingParking(final boolean seekingParking) {
        this.seekingParking = seekingParking;
    }
}
