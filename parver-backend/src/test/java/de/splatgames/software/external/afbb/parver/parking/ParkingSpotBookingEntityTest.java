package de.splatgames.software.external.afbb.parver.parking;

import de.splatgames.software.external.afbb.parver.TestEntityFactory;
import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParkingSpotBookingEntity")
class ParkingSpotBookingEntityTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 3, 5, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 3, 5, 17, 0);

    private ParkingSpotBookingEntity booking;

    @BeforeEach
    void setUp() {
        final ParkingSpotEntity spot = TestEntityFactory.createSpot(53, "small");
        final UserEntity seeker = TestEntityFactory.createUser(2L, "seeker", "Seeker", UserRole.USER);
        final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(1L, spot,
                START.minusHours(1), END.plusHours(1));
        booking = TestEntityFactory.createBooking(1L, release, seeker, START, END);
    }

    @Nested
    @DisplayName("containsTime")
    class ContainsTime {

        @Test
        @DisplayName("returns true for exact start time")
        void exactStart() {
            assertThat(booking.containsTime(START)).isTrue();
        }

        @Test
        @DisplayName("returns true for exact end time")
        void exactEnd() {
            assertThat(booking.containsTime(END)).isTrue();
        }

        @Test
        @DisplayName("returns true for time in the middle")
        void middle() {
            assertThat(booking.containsTime(START.plusHours(4))).isTrue();
        }

        @Test
        @DisplayName("returns false for time before start")
        void beforeStart() {
            assertThat(booking.containsTime(START.minusMinutes(1))).isFalse();
        }

        @Test
        @DisplayName("returns false for time after end")
        void afterEnd() {
            assertThat(booking.containsTime(END.plusMinutes(1))).isFalse();
        }
    }

    @Nested
    @DisplayName("overlaps")
    class Overlaps {

        @Test
        @DisplayName("returns true for partial overlap")
        void partialOverlap() {
            assertThat(booking.overlaps(START.plusHours(1), END.plusHours(1))).isTrue();
        }

        @Test
        @DisplayName("returns true for contained interval")
        void contained() {
            assertThat(booking.overlaps(START.plusHours(1), END.minusHours(1))).isTrue();
        }

        @Test
        @DisplayName("returns false for interval completely before")
        void completelyBefore() {
            assertThat(booking.overlaps(START.minusHours(5), START.minusHours(1))).isFalse();
        }

        @Test
        @DisplayName("returns false for interval completely after")
        void completelyAfter() {
            assertThat(booking.overlaps(END.plusHours(1), END.plusHours(5))).isFalse();
        }

        @Test
        @DisplayName("returns false for touching at boundary")
        void touchingBoundary() {
            assertThat(booking.overlaps(END, END.plusHours(2))).isFalse();
            assertThat(booking.overlaps(START.minusHours(2), START)).isFalse();
        }
    }
}
