package de.splatgames.software.external.afbb.parver;

import de.splatgames.software.external.afbb.parver.notification.PushSubscriptionEntity;
import de.splatgames.software.external.afbb.parver.parking.ParkingSpotBookingEntity;
import de.splatgames.software.external.afbb.parver.parking.ParkingSpotEntity;
import de.splatgames.software.external.afbb.parver.parking.ParkingSpotReleaseEntity;
import de.splatgames.software.external.afbb.parver.parking.ParkingSpotReportEntity;
import de.splatgames.software.external.afbb.parver.parking.ReportStatus;
import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserRole;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

/**
 * Factory for creating test entities with IDs set via reflection.
 */
public final class TestEntityFactory {

    private TestEntityFactory() {
    }

    public static UserEntity createUser(final long id, final String username,
                                         final String displayName, final UserRole role) {
        final var user = new UserEntity(username, displayName, "hashed-password", role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static ParkingSpotEntity createSpot(final int spotNumber, final String area) {
        return new ParkingSpotEntity(spotNumber, area);
    }

    public static ParkingSpotEntity createSpotWithOwner(final int spotNumber, final String area,
                                                         final UserEntity owner) {
        final var spot = new ParkingSpotEntity(spotNumber, area);
        spot.setOwner(owner);
        return spot;
    }

    public static ParkingSpotReleaseEntity createRelease(final long id,
                                                          final ParkingSpotEntity spot,
                                                          final LocalDateTime from,
                                                          final LocalDateTime to) {
        final var release = new ParkingSpotReleaseEntity(spot, from, to);
        ReflectionTestUtils.setField(release, "id", id);
        return release;
    }

    public static ParkingSpotBookingEntity createBooking(final long id,
                                                          final ParkingSpotReleaseEntity release,
                                                          final UserEntity bookedBy,
                                                          final LocalDateTime from,
                                                          final LocalDateTime to) {
        final var booking = new ParkingSpotBookingEntity(release, bookedBy, from, to);
        ReflectionTestUtils.setField(booking, "id", id);
        return booking;
    }

    public static ParkingSpotReportEntity createReport(final long id,
                                                        final ParkingSpotEntity spot,
                                                        final UserEntity reporter,
                                                        final String comment) {
        final var report = new ParkingSpotReportEntity(spot, reporter, comment);
        ReflectionTestUtils.setField(report, "id", id);
        return report;
    }

    public static PushSubscriptionEntity createPushSubscription(final long id,
                                                                  final UserEntity user,
                                                                  final boolean seekingParking) {
        final var sub = new PushSubscriptionEntity(user, "https://push.example.com/endpoint",
                "p256dh-key", "auth-key", seekingParking);
        ReflectionTestUtils.setField(sub, "id", id);
        return sub;
    }
}
