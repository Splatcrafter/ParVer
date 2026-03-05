package de.splatgames.software.external.afbb.parver.notification;

import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserRepository;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class PushNotificationServiceImpl implements PushNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(PushNotificationServiceImpl.class);

    private final PushSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PushService pushService;
    private final String vapidPublicKey;

    public PushNotificationServiceImpl(
            @NotNull final PushSubscriptionRepository subscriptionRepository,
            @NotNull final UserRepository userRepository,
            @Value("${parver.push.vapid.public-key}") @NotNull final String vapidPublicKey,
            @Value("${parver.push.vapid.private-key}") @NotNull final String vapidPrivateKey,
            @Value("${parver.push.vapid.subject}") @NotNull final String vapidSubject) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.vapidPublicKey = vapidPublicKey;

        // Register BouncyCastle if not already present
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }

        try {
            this.pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize PushService with VAPID keys", e);
        }
    }

    @Override
    public void subscribe(final long userId, @NotNull final String endpoint,
                          @NotNull final String p256dh, @NotNull final String auth,
                          final boolean seekingParking) {
        final UserEntity user = this.userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        final PushSubscriptionEntity subscription = this.subscriptionRepository.findByUserId(userId)
                .map(existing -> {
                    existing.setEndpoint(endpoint);
                    existing.setP256dh(p256dh);
                    existing.setAuth(auth);
                    existing.setSeekingParking(seekingParking);
                    return existing;
                })
                .orElseGet(() -> new PushSubscriptionEntity(user, endpoint, p256dh, auth, seekingParking));

        this.subscriptionRepository.save(subscription);
    }

    @Override
    public void unsubscribe(final long userId) {
        this.subscriptionRepository.deleteByUserId(userId);
    }

    @Override
    public void setSeeking(final long userId, final boolean seeking) {
        this.subscriptionRepository.findByUserId(userId)
                .ifPresent(sub -> {
                    sub.setSeekingParking(seeking);
                    this.subscriptionRepository.save(sub);
                });
    }

    @Override
    @NotNull
    public Optional<PushSubscriptionEntity> getSubscription(final long userId) {
        return this.subscriptionRepository.findByUserId(userId);
    }

    @Override
    @NotNull
    public String getVapidPublicKey() {
        return this.vapidPublicKey;
    }

    @Override
    @Async
    public void notifySeekingUsers(@NotNull final String title, @NotNull final String body) {
        final List<PushSubscriptionEntity> seekingSubs =
                this.subscriptionRepository.findAllBySeekingParkingTrue();

        final String payload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"url\":\"/\"}",
                escapeJson(title), escapeJson(body));

        for (final PushSubscriptionEntity sub : seekingSubs) {
            try {
                final Notification notification = new Notification(
                        sub.getEndpoint(),
                        sub.getP256dh(),
                        sub.getAuth(),
                        payload.getBytes());

                final var response = this.pushService.send(notification, Encoding.AES128GCM);
                final int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 410 || statusCode == 404) {
                    LOG.info("Push subscription expired for user {}, removing", sub.getUser().getUsername());
                    this.subscriptionRepository.delete(sub);
                } else if (statusCode >= 400) {
                    LOG.warn("Failed to send push to user {}: HTTP {}",
                            sub.getUser().getUsername(), statusCode);
                }
            } catch (final Exception e) {
                LOG.error("Error sending push notification to user {}: {}",
                        sub.getUser().getUsername(), e.getMessage());
            }
        }
    }

    private static String escapeJson(@NotNull final String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
