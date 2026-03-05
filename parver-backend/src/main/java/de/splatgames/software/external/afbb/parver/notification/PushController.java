package de.splatgames.software.external.afbb.parver.notification;

import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final PushNotificationService pushNotificationService;
    private final UserService userService;

    public PushController(
            @NotNull final PushNotificationService pushNotificationService,
            @NotNull final UserService userService) {
        this.pushNotificationService = pushNotificationService;
        this.userService = userService;
    }

    @GetMapping("/vapid-key")
    public ResponseEntity<Map<String, String>> getVapidKey() {
        return ResponseEntity.ok(Map.of("publicKey", this.pushNotificationService.getVapidPublicKey()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody @NotNull final SubscribeRequest request) {
        final long userId = getAuthenticatedUserId();
        this.pushNotificationService.subscribe(
                userId, request.endpoint(), request.p256dh(), request.auth(), request.seekingParking());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<Void> unsubscribe() {
        final long userId = getAuthenticatedUserId();
        this.pushNotificationService.unsubscribe(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/seeking")
    public ResponseEntity<Void> updateSeeking(@RequestBody @NotNull final SeekingRequest request) {
        final long userId = getAuthenticatedUserId();
        this.pushNotificationService.setSeeking(userId, request.seekingParking());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        final long userId = getAuthenticatedUserId();
        return this.pushNotificationService.getSubscription(userId)
                .map(sub -> ResponseEntity.ok(Map.<String, Object>of(
                        "subscribed", true,
                        "seekingParking", sub.isSeekingParking())))
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "subscribed", false,
                        "seekingParking", false)));
    }

    private long getAuthenticatedUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String username = auth.getName();
        final UserEntity user = this.userService.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found"));
        return user.getId();
    }

    public record SubscribeRequest(
            @NotNull String endpoint,
            @NotNull String p256dh,
            @NotNull String auth,
            boolean seekingParking) {
    }

    public record SeekingRequest(boolean seekingParking) {
    }
}
