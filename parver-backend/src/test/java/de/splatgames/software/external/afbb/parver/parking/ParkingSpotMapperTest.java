package de.splatgames.software.external.afbb.parver.parking;

import de.splatgames.software.external.afbb.parver.TestEntityFactory;
import de.splatgames.software.external.afbb.parver.model.ParkingSpace;
import de.splatgames.software.external.afbb.parver.model.ParkingSpotBooking;
import de.splatgames.software.external.afbb.parver.model.ParkingSpotRelease;
import de.splatgames.software.external.afbb.parver.model.ParkingSpotReport;
import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParkingSpotMapper")
class ParkingSpotMapperTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 5, 12, 0);
    private static final ZoneId APP_ZONE = ZoneId.of("Europe/Berlin");

    private UserEntity owner;
    private UserEntity booker;
    private ParkingSpotEntity spot;

    @BeforeEach
    void setUp() {
        owner = TestEntityFactory.createUser(1L, "owner", "Owner Name", UserRole.USER);
        booker = TestEntityFactory.createUser(2L, "booker", "Booker Name", UserRole.USER);
        spot = TestEntityFactory.createSpotWithOwner(53, "small", owner);
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("maps INACTIVE spot without owner correctly")
        void inactive() {
            final ParkingSpotEntity noOwnerSpot = TestEntityFactory.createSpot(10, "large");

            final ParkingSpace result = ParkingSpotMapper.toResponse(
                    noOwnerSpot, ParkingSpotStatus.INACTIVE, Collections.emptyList(), NOW);

            assertThat(result.getSpotNumber()).isEqualTo(10);
            assertThat(result.getArea()).isEqualTo(ParkingSpace.AreaEnum.LARGE);
            assertThat(result.getStatus()).isEqualTo(ParkingSpace.StatusEnum.INACTIVE);
            assertThat(result.getOwnerDisplayName()).isNull();
            assertThat(result.getOwnerId()).isNull();
            assertThat(result.getCurrentBookerDisplayName()).isNull();
            assertThat(result.getActiveReleases()).isEmpty();
        }

        @Test
        @DisplayName("maps OCCUPIED spot with owner correctly")
        void occupied() {
            final ParkingSpace result = ParkingSpotMapper.toResponse(
                    spot, ParkingSpotStatus.OCCUPIED, Collections.emptyList(), NOW);

            assertThat(result.getSpotNumber()).isEqualTo(53);
            assertThat(result.getArea()).isEqualTo(ParkingSpace.AreaEnum.SMALL);
            assertThat(result.getStatus()).isEqualTo(ParkingSpace.StatusEnum.OCCUPIED);
            assertThat(result.getOwnerDisplayName()).isEqualTo("Owner Name");
            assertThat(result.getOwnerId()).isEqualTo(1L);
            assertThat(result.getCurrentBookerDisplayName()).isNull();
        }

        @Test
        @DisplayName("maps AVAILABLE spot with active releases")
        void available() {
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(
                    1L, spot, NOW.minusHours(2), NOW.plusHours(6));

            final ParkingSpace result = ParkingSpotMapper.toResponse(
                    spot, ParkingSpotStatus.AVAILABLE, List.of(release), NOW);

            assertThat(result.getStatus()).isEqualTo(ParkingSpace.StatusEnum.AVAILABLE);
            assertThat(result.getActiveReleases()).hasSize(1);
            assertThat(result.getCurrentBookerDisplayName()).isNull();
        }

        @Test
        @DisplayName("maps BOOKED spot with current booker display name")
        void booked() {
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(
                    1L, spot, NOW.minusHours(2), NOW.plusHours(6));
            final ParkingSpotBookingEntity booking = TestEntityFactory.createBooking(
                    1L, release, booker, NOW.minusHours(1), NOW.plusHours(3));
            release.getBookings().add(booking);

            final ParkingSpace result = ParkingSpotMapper.toResponse(
                    spot, ParkingSpotStatus.BOOKED, List.of(release), NOW);

            assertThat(result.getStatus()).isEqualTo(ParkingSpace.StatusEnum.BOOKED);
            assertThat(result.getCurrentBookerDisplayName()).isEqualTo("Booker Name");
        }

        @Test
        @DisplayName("BOOKED status but no booking covering 'now' leaves booker null")
        void bookedNoCurrentBooking() {
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(
                    1L, spot, NOW.minusHours(5), NOW.plusHours(6));
            // Booking in the past, not covering NOW
            final ParkingSpotBookingEntity booking = TestEntityFactory.createBooking(
                    1L, release, booker, NOW.minusHours(5), NOW.minusHours(3));
            release.getBookings().add(booking);

            final ParkingSpace result = ParkingSpotMapper.toResponse(
                    spot, ParkingSpotStatus.BOOKED, List.of(release), NOW);

            assertThat(result.getCurrentBookerDisplayName()).isNull();
        }
    }

    @Nested
    @DisplayName("toReleaseResponse")
    class ToReleaseResponse {

        @Test
        @DisplayName("maps all fields correctly")
        void mapsFields() {
            final LocalDateTime from = LocalDateTime.of(2026, 3, 5, 8, 0);
            final LocalDateTime to = LocalDateTime.of(2026, 3, 5, 18, 0);
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(42L, spot, from, to);

            final ParkingSpotRelease dto = ParkingSpotMapper.toReleaseResponse(release);

            assertThat(dto.getId()).isEqualTo(42L);
            assertThat(dto.getSpotNumber()).isEqualTo(53);
            assertThat(dto.getAvailableFrom()).isNotNull();
            assertThat(dto.getAvailableTo()).isNotNull();
            assertThat(dto.getBookings()).isEmpty();
        }

        @Test
        @DisplayName("includes bookings in response")
        void includesBookings() {
            final LocalDateTime from = LocalDateTime.of(2026, 3, 5, 8, 0);
            final LocalDateTime to = LocalDateTime.of(2026, 3, 5, 18, 0);
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(1L, spot, from, to);
            final ParkingSpotBookingEntity booking = TestEntityFactory.createBooking(
                    10L, release, booker, from.plusHours(1), to.minusHours(1));
            release.getBookings().add(booking);

            final ParkingSpotRelease dto = ParkingSpotMapper.toReleaseResponse(release);

            assertThat(dto.getBookings()).hasSize(1);
            assertThat(dto.getBookings().get(0).getId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("toBookingResponse")
    class ToBookingResponse {

        @Test
        @DisplayName("maps all fields correctly")
        void mapsFields() {
            final LocalDateTime from = LocalDateTime.of(2026, 3, 5, 9, 0);
            final LocalDateTime to = LocalDateTime.of(2026, 3, 5, 17, 0);
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(
                    1L, spot, from.minusHours(1), to.plusHours(1));
            final ParkingSpotBookingEntity booking = TestEntityFactory.createBooking(
                    7L, release, booker, from, to);

            final ParkingSpotBooking dto = ParkingSpotMapper.toBookingResponse(booking);

            assertThat(dto.getId()).isEqualTo(7L);
            assertThat(dto.getSpotNumber()).isEqualTo(53);
            assertThat(dto.getBookedByDisplayName()).isEqualTo("Booker Name");
            assertThat(dto.getBookedById()).isEqualTo(2L);
            assertThat(dto.getBookedFrom()).isNotNull();
            assertThat(dto.getBookedTo()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toAppLocalDateTime")
    class ToAppLocalDateTime {

        @Test
        @DisplayName("converts UTC OffsetDateTime to Europe/Berlin LocalDateTime")
        void utcToBerlin() {
            // UTC 10:00 → Berlin CET is UTC+1 → 11:00
            final OffsetDateTime utc = OffsetDateTime.of(2026, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);
            final LocalDateTime result = ParkingSpotMapper.toAppLocalDateTime(utc);

            // January = CET (UTC+1)
            assertThat(result).isEqualTo(LocalDateTime.of(2026, 1, 15, 11, 0));
        }

        @Test
        @DisplayName("handles summer time (CEST = UTC+2) correctly")
        void summerTime() {
            // UTC 10:00 in July → Berlin CEST is UTC+2 → 12:00
            final OffsetDateTime utc = OffsetDateTime.of(2026, 7, 15, 10, 0, 0, 0, ZoneOffset.UTC);
            final LocalDateTime result = ParkingSpotMapper.toAppLocalDateTime(utc);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 7, 15, 12, 0));
        }

        @Test
        @DisplayName("converts non-UTC offset correctly")
        void nonUtcOffset() {
            // +05:00 14:00 → UTC 09:00 → Berlin CET 10:00
            final OffsetDateTime offset = OffsetDateTime.of(2026, 1, 15, 14, 0, 0, 0,
                    ZoneOffset.ofHours(5));
            final LocalDateTime result = ParkingSpotMapper.toAppLocalDateTime(offset);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 0));
        }

        @Test
        @DisplayName("same-zone offset passes through unchanged")
        void sameZone() {
            // Already in CET (+01:00) in January
            final OffsetDateTime cet = OffsetDateTime.of(2026, 1, 15, 10, 0, 0, 0,
                    ZoneOffset.ofHours(1));
            final LocalDateTime result = ParkingSpotMapper.toAppLocalDateTime(cet);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 0));
        }
    }

    @Nested
    @DisplayName("toReportResponse")
    class ToReportResponse {

        @Test
        @DisplayName("maps all fields correctly")
        void mapsFields() {
            final ParkingSpotReportEntity report = TestEntityFactory.createReport(
                    5L, spot, owner, "Parkplatz ist blockiert");
            // Simulate @PrePersist
            ReflectionTestUtils.setField(report, "createdAt", Instant.parse("2026-03-05T10:00:00Z"));

            final ParkingSpotReport dto = ParkingSpotMapper.toReportResponse(report);

            assertThat(dto.getId()).isEqualTo(5L);
            assertThat(dto.getSpotNumber()).isEqualTo(53);
            assertThat(dto.getReporterDisplayName()).isEqualTo("Owner Name");
            assertThat(dto.getReporterId()).isEqualTo(1L);
            assertThat(dto.getComment()).isEqualTo("Parkplatz ist blockiert");
            assertThat(dto.getStatus()).isEqualTo(ParkingSpotReport.StatusEnum.OPEN);
            assertThat(dto.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("maps null comment correctly")
        void nullComment() {
            final ParkingSpotReportEntity report = TestEntityFactory.createReport(
                    6L, spot, owner, null);
            ReflectionTestUtils.setField(report, "createdAt", Instant.parse("2026-03-05T10:00:00Z"));

            final ParkingSpotReport dto = ParkingSpotMapper.toReportResponse(report);

            assertThat(dto.getComment()).isNull();
        }

        @Test
        @DisplayName("maps RESOLVED status correctly")
        void resolvedStatus() {
            final ParkingSpotReportEntity report = TestEntityFactory.createReport(
                    7L, spot, owner, "Fixed");
            report.setStatus(ReportStatus.RESOLVED);
            ReflectionTestUtils.setField(report, "createdAt", Instant.parse("2026-03-05T10:00:00Z"));

            final ParkingSpotReport dto = ParkingSpotMapper.toReportResponse(report);

            assertThat(dto.getStatus()).isEqualTo(ParkingSpotReport.StatusEnum.RESOLVED);
        }
    }
}
