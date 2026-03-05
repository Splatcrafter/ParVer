package de.splatgames.software.external.afbb.parver.notification;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PushNotificationService {

    void subscribe(long userId, @NotNull String endpoint, @NotNull String p256dh,
                   @NotNull String auth, boolean seekingParking);

    void unsubscribe(long userId);

    void setSeeking(long userId, boolean seeking);

    @NotNull
    Optional<PushSubscriptionEntity> getSubscription(long userId);

    @NotNull
    String getVapidPublicKey();

    void notifySeekingUsers(@NotNull String title, @NotNull String body);
}
