package de.splatgames.software.external.afbb.parver.parking;

import de.splatgames.software.external.afbb.parver.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParkingSpotReleaseEntity")
class ParkingSpotReleaseEntityTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 3, 5, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 3, 5, 18, 0);

    private ParkingSpotReleaseEntity release;

    @BeforeEach
    void setUp() {
        final ParkingSpotEntity spot = TestEntityFactory.createSpot(53, "small");
        release = TestEntityFactory.createRelease(1L, spot, START, END);
    }

    @Nested
    @DisplayName("containsTime")
    class ContainsTime {

        @Test
        @DisplayName("returns true for exact start time")
        void exactStart() {
            assertThat(release.containsTime(START)).isTrue();
        }

        @Test
        @DisplayName("returns true for exact end time")
        void exactEnd() {
            assertThat(release.containsTime(END)).isTrue();
        }

        @Test
        @DisplayName("returns true for time in the middle")
        void middle() {
            assertThat(release.containsTime(START.plusHours(5))).isTrue();
        }

        @Test
        @DisplayName("returns false for time before start")
        void beforeStart() {
            assertThat(release.containsTime(START.minusMinutes(1))).isFalse();
        }

        @Test
        @DisplayName("returns false for time after end")
        void afterEnd() {
            assertThat(release.containsTime(END.plusMinutes(1))).isFalse();
        }
    }

    @Nested
    @DisplayName("overlaps")
    class Overlaps {

        @Test
        @DisplayName("returns true for partial overlap at start")
        void partialOverlapStart() {
            assertThat(release.overlaps(START.minusHours(1), START.plusHours(1))).isTrue();
        }

        @Test
        @DisplayName("returns true for partial overlap at end")
        void partialOverlapEnd() {
            assertThat(release.overlaps(END.minusHours(1), END.plusHours(1))).isTrue();
        }

        @Test
        @DisplayName("returns true for contained interval")
        void contained() {
            assertThat(release.overlaps(START.plusHours(1), END.minusHours(1))).isTrue();
        }

        @Test
        @DisplayName("returns true for encompassing interval")
        void encompassing() {
            assertThat(release.overlaps(START.minusHours(1), END.plusHours(1))).isTrue();
        }

        @Test
        @DisplayName("returns false for interval completely before")
        void completelyBefore() {
            assertThat(release.overlaps(START.minusHours(5), START.minusHours(1))).isFalse();
        }

        @Test
        @DisplayName("returns false for interval completely after")
        void completelyAfter() {
            assertThat(release.overlaps(END.plusHours(1), END.plusHours(5))).isFalse();
        }

        @Test
        @DisplayName("returns false for touching at end (adjacent, not overlapping)")
        void touchingAtEnd() {
            // from.isBefore(availableTo) → END.isBefore(END) → false
            assertThat(release.overlaps(END, END.plusHours(5))).isFalse();
        }

        @Test
        @DisplayName("returns false for touching at start (adjacent, not overlapping)")
        void touchingAtStart() {
            // to.isAfter(availableFrom) → START.isAfter(START) → false
            assertThat(release.overlaps(START.minusHours(5), START)).isFalse();
        }
    }
}
