package de.splatgames.software.external.afbb.parver.notification;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscriptionEntity, Long> {

    @NotNull
    Optional<PushSubscriptionEntity> findByUserId(long userId);

    @NotNull
    List<PushSubscriptionEntity> findAllBySeekingParkingTrue();

    void deleteByUserId(long userId);
}
