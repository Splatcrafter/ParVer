package de.splatgames.software.external.afbb.parver.parking;

import de.splatgames.software.external.afbb.parver.TestEntityFactory;
import de.splatgames.software.external.afbb.parver.notification.PushNotificationService;
import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserRepository;
import de.splatgames.software.external.afbb.parver.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingSpotServiceImpl")
class ParkingSpotServiceImplTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private ParkingSpotReleaseRepository releaseRepository;
    @Mock
    private ParkingSpotBookingRepository bookingRepository;
    @Mock
    private ParkingSpotReportRepository reportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SseEmitterService sseEmitterService;
    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private ParkingSpotServiceImpl service;

    private UserEntity owner;
    private UserEntity seeker;
    private ParkingSpotEntity spot;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 5, 10, 0);
    private static final LocalDateTime TOMORROW = NOW.plusDays(1);
    private static final LocalDateTime YESTERDAY = NOW.minusDays(1);

    @BeforeEach
    void setUp() {
        owner = TestEntityFactory.createUser(1L, "owner", "Owner", UserRole.USER);
        seeker = TestEntityFactory.createUser(2L, "seeker", "Seeker", UserRole.USER);
        spot = TestEntityFactory.createSpotWithOwner(53, "small", owner);
    }

    // ===================== Spot Management =====================

    @Nested
    @DisplayName("createSpot")
    class CreateSpot {

        @Test
        @DisplayName("creates new parking spot")
        void success() {
            when(parkingSpotRepository.existsById(99)).thenReturn(false);
            when(parkingSpotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ParkingSpotEntity result = service.createSpot(99, "large");

            assertThat(result.getSpotNumber()).isEqualTo(99);
            assertThat(result.getArea()).isEqualTo("large");
        }

        @Test
        @DisplayName("throws IllegalArgumentException for duplicate spot number")
        void duplicate() {
            when(parkingSpotRepository.existsById(53)).thenReturn(true);

            assertThatThrownBy(() -> service.createSpot(53, "small"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("findAll / findBySpotNumber / findByOwnerId")
    class FindOperations {

        @Test
        @DisplayName("findAll returns all spots")
        void findAll() {
            when(parkingSpotRepository.findAll()).thenReturn(List.of(spot));
            assertThat(service.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("findBySpotNumber returns spot when exists")
        void findBySpotNumber_exists() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            assertThat(service.findBySpotNumber(53)).isPresent();
        }

        @Test
        @DisplayName("findBySpotNumber returns empty when not exists")
        void findBySpotNumber_notExists() {
            when(parkingSpotRepository.findById(999)).thenReturn(Optional.empty());
            assertThat(service.findBySpotNumber(999)).isEmpty();
        }
    }

    // ===================== Owner Assignment =====================

    @Nested
    @DisplayName("assignOwner")
    class AssignOwner {

        @Test
        @DisplayName("assigns owner to spot")
        void success() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(parkingSpotRepository.findByOwnerId(2L)).thenReturn(Optional.empty());

            service.assignOwner(53, 2L);

            assertThat(spot.getOwner()).isEqualTo(seeker);
            verify(parkingSpotRepository).save(spot);
        }

        @Test
        @DisplayName("clears previous spot ownership when user already owns another spot")
        void clearsPreviousOwnership() {
            final ParkingSpotEntity otherSpot = TestEntityFactory.createSpotWithOwner(54, "small", seeker);
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(parkingSpotRepository.findByOwnerId(2L)).thenReturn(Optional.of(otherSpot));

            service.assignOwner(53, 2L);

            assertThat(otherSpot.getOwner()).isNull();
            assertThat(spot.getOwner()).isEqualTo(seeker);
        }

        @Test
        @DisplayName("throws NoSuchElementException when spot not found")
        void spotNotFound() {
            when(parkingSpotRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.assignOwner(999, 1L))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("throws NoSuchElementException when user not found")
        void userNotFound() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.assignOwner(53, 999L))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("removeOwner")
    class RemoveOwner {

        @Test
        @DisplayName("sets owner to null")
        void success() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));

            service.removeOwner(53);

            assertThat(spot.getOwner()).isNull();
            verify(parkingSpotRepository).save(spot);
        }

        @Test
        @DisplayName("throws NoSuchElementException when spot not found")
        void spotNotFound() {
            when(parkingSpotRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removeOwner(999))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // ===================== Release Management =====================

    @Nested
    @DisplayName("createRelease")
    class CreateRelease {

        @Test
        @DisplayName("creates release successfully and broadcasts update")
        void success() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            when(releaseRepository.findOverlapping(eq(53), any(), any())).thenReturn(Collections.emptyList());
            when(releaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            // For broadcastParkingUpdate
            when(parkingSpotRepository.findAll()).thenReturn(List.of(spot));
            when(releaseRepository.findByParkingSpotSpotNumberAndAvailableToAfter(anyInt(), any()))
                    .thenReturn(Collections.emptyList());
            when(releaseRepository.findByParkingSpotSpotNumber(anyInt())).thenReturn(Collections.emptyList());

            final ParkingSpotReleaseEntity result = service.createRelease(53, 1L, NOW, TOMORROW);

            assertThat(result.getAvailableFrom()).isEqualTo(NOW);
            assertThat(result.getAvailableTo()).isEqualTo(TOMORROW);
            verify(pushNotificationService).notifySeekingUsers(anyString(), anyString());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when from is after to")
        void fromAfterTo() {
            assertThatThrownBy(() -> service.createRelease(53, 1L, TOMORROW, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("before");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when from equals to")
        void fromEqualsTo() {
            assertThatThrownBy(() -> service.createRelease(53, 1L, NOW, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("before");
        }

        @Test
        @DisplayName("throws NoSuchElementException when spot not found")
        void spotNotFound() {
            when(parkingSpotRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createRelease(999, 1L, NOW, TOMORROW))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("throws SecurityException when user is not the owner")
        void notOwner() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));

            assertThatThrownBy(() -> service.createRelease(53, 2L, NOW, TOMORROW))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("owner");
        }

        @Test
        @DisplayName("throws SecurityException when spot has no owner")
        void noOwner() {
            spot.setOwner(null);
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));

            assertThatThrownBy(() -> service.createRelease(53, 1L, NOW, TOMORROW))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when release overlaps with existing")
        void overlapping() {
            final ParkingSpotReleaseEntity existing = TestEntityFactory.createRelease(10L, spot, NOW, TOMORROW);
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            when(releaseRepository.findOverlapping(eq(53), any(), any())).thenReturn(List.of(existing));

            assertThatThrownBy(() -> service.createRelease(53, 1L,
                    NOW.plusHours(1), TOMORROW.plusHours(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("overlaps");
        }
    }

    @Nested
    @DisplayName("deleteRelease")
    class DeleteRelease {

        @Test
        @DisplayName("deletes release when user is owner")
        void success() {
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(10L, spot, NOW, TOMORROW);
            when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));
            when(parkingSpotRepository.findAll()).thenReturn(List.of(spot));
            when(releaseRepository.findByParkingSpotSpotNumberAndAvailableToAfter(anyInt(), any()))
                    .thenReturn(Collections.emptyList());
            when(releaseRepository.findByParkingSpotSpotNumber(anyInt())).thenReturn(Collections.emptyList());

            service.deleteRelease(10L, 1L);

            verify(releaseRepository).delete(release);
        }

        @Test
        @DisplayName("throws NoSuchElementException when release not found")
        void notFound() {
            when(releaseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteRelease(999L, 1L))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("throws SecurityException when user is not owner")
        void notOwner() {
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(10L, spot, NOW, TOMORROW);
            when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));

            assertThatThrownBy(() -> service.deleteRelease(10L, 2L))
                    .isInstanceOf(SecurityException.class);
        }
    }

    @Test
    @DisplayName("clearSpotData deletes all releases for a spot")
    void clearSpotData() {
        final ParkingSpotReleaseEntity r1 = TestEntityFactory.createRelease(1L, spot, NOW, TOMORROW);
        final ParkingSpotReleaseEntity r2 = TestEntityFactory.createRelease(2L, spot, TOMORROW, TOMORROW.plusDays(1));
        when(releaseRepository.findByParkingSpotSpotNumber(53)).thenReturn(List.of(r1, r2));

        service.clearSpotData(53);

        verify(releaseRepository).deleteAll(List.of(r1, r2));
    }

    @Test
    @DisplayName("deleteBookingsByUser deletes all bookings by user")
    void deleteBookingsByUser() {
        final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(1L, spot, NOW, TOMORROW);
        final ParkingSpotBookingEntity booking = TestEntityFactory.createBooking(1L, release, seeker, NOW, TOMORROW);
        when(bookingRepository.findByBookedById(2L)).thenReturn(List.of(booking));

        service.deleteBookingsByUser(2L);

        verify(bookingRepository).deleteAll(List.of(booking));
    }

    // ===================== Booking Management =====================

    @Nested
    @DisplayName("createBooking")
    class CreateBooking {

        private ParkingSpotReleaseEntity release;

        @BeforeEach
        void setUpRelease() {
            release = TestEntityFactory.createRelease(10L, spot, NOW, TOMORROW);
        }

        @Test
        @DisplayName("creates booking within release window")
        void success() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));
            when(bookingRepository.findOverlapping(eq(10L), any(), any())).thenReturn(Collections.emptyList());
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(parkingSpotRepository.findAll()).thenReturn(List.of(spot));
            when(releaseRepository.findByParkingSpotSpotNumberAndAvailableToAfter(anyInt(), any()))
                    .thenReturn(Collections.emptyList());
            when(releaseRepository.findByParkingSpotSpotNumber(anyInt())).thenReturn(Collections.emptyList());

            final ParkingSpotBookingEntity result = service.createBooking(53, 10L, 2L,
                    NOW.plusHours(1), NOW.plusHours(5));

            assertThat(result.getBookedBy()).isEqualTo(seeker);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when from is after to")
        void fromAfterTo() {
            assertThatThrownBy(() -> service.createBooking(53, 10L, 2L, TOMORROW, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("before");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when from equals to")
        void fromEqualsTo() {
            assertThatThrownBy(() -> service.createBooking(53, 10L, 2L, NOW, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("before");
        }

        @Test
        @DisplayName("throws NoSuchElementException when user not found")
        void userNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createBooking(53, 10L, 999L, NOW, TOMORROW))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("throws SecurityException when user owns a parking spot")
        void userOwnsParkingSpot() {
            // seeker now also has a parking spot
            final ParkingSpotEntity seekerSpot = TestEntityFactory.createSpotWithOwner(54, "small", seeker);
            // We need to set the parkingSpot field on seeker via the bidirectional relationship
            // hasParkingSpot() checks this.parkingSpot != null
            org.springframework.test.util.ReflectionTestUtils.setField(seeker, "parkingSpot", seekerSpot);

            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));

            assertThatThrownBy(() -> service.createBooking(53, 10L, 2L, NOW, TOMORROW))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("own parking spot");
        }

        @Test
        @DisplayName("throws NoSuchElementException when release not found")
        void releaseNotFound() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(releaseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createBooking(53, 999L, 2L, NOW, TOMORROW))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when release does not belong to spot")
        void releaseDoesNotBelongToSpot() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));

            assertThatThrownBy(() -> service.createBooking(99, 10L, 2L, NOW, TOMORROW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when booking starts before release")
        void bookingBeforeRelease() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));

            assertThatThrownBy(() -> service.createBooking(53, 10L, 2L,
                    NOW.minusHours(1), NOW.plusHours(5)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("within the release window");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when booking ends after release")
        void bookingAfterRelease() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));

            assertThatThrownBy(() -> service.createBooking(53, 10L, 2L,
                    NOW.plusHours(1), TOMORROW.plusHours(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("within the release window");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when booking overlaps existing booking")
        void overlapping() {
            final ParkingSpotBookingEntity existing = TestEntityFactory.createBooking(
                    1L, release, seeker, NOW.plusHours(2), NOW.plusHours(6));

            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));
            when(bookingRepository.findOverlapping(eq(10L), any(), any())).thenReturn(List.of(existing));

            assertThatThrownBy(() -> service.createBooking(53, 10L, 2L,
                    NOW.plusHours(3), NOW.plusHours(8)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("overlaps");
        }
    }

    @Nested
    @DisplayName("deleteBooking")
    class DeleteBooking {

        private ParkingSpotReleaseEntity release;
        private ParkingSpotBookingEntity booking;

        @BeforeEach
        void setUpBooking() {
            release = TestEntityFactory.createRelease(10L, spot, NOW, TOMORROW);
            booking = TestEntityFactory.createBooking(1L, release, seeker, NOW, TOMORROW);
        }

        @Test
        @DisplayName("deletes booking when user is the booker")
        void deleteByBooker() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(parkingSpotRepository.findAll()).thenReturn(List.of(spot));
            when(releaseRepository.findByParkingSpotSpotNumberAndAvailableToAfter(anyInt(), any()))
                    .thenReturn(Collections.emptyList());
            when(releaseRepository.findByParkingSpotSpotNumber(anyInt())).thenReturn(Collections.emptyList());

            service.deleteBooking(1L, 2L, false);

            verify(bookingRepository).delete(booking);
        }

        @Test
        @DisplayName("deletes booking when user is admin")
        void deleteByAdmin() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(parkingSpotRepository.findAll()).thenReturn(List.of(spot));
            when(releaseRepository.findByParkingSpotSpotNumberAndAvailableToAfter(anyInt(), any()))
                    .thenReturn(Collections.emptyList());
            when(releaseRepository.findByParkingSpotSpotNumber(anyInt())).thenReturn(Collections.emptyList());

            service.deleteBooking(1L, 999L, true); // different user, but admin

            verify(bookingRepository).delete(booking);
        }

        @Test
        @DisplayName("throws SecurityException when user is neither booker nor admin")
        void unauthorized() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> service.deleteBooking(1L, 999L, false))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("throws NoSuchElementException when booking not found")
        void notFound() {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteBooking(999L, 2L, false))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // ===================== Status Computation =====================

    @Nested
    @DisplayName("computeStatus")
    class ComputeStatus {

        @Test
        @DisplayName("returns INACTIVE when spot has no owner")
        void inactive() {
            spot.setOwner(null);
            assertThat(service.computeStatus(spot, NOW)).isEqualTo(ParkingSpotStatus.INACTIVE);
        }

        @Test
        @DisplayName("returns OCCUPIED when owner has no active release")
        void occupied() {
            when(releaseRepository.findByParkingSpotSpotNumber(53)).thenReturn(Collections.emptyList());

            assertThat(service.computeStatus(spot, NOW)).isEqualTo(ParkingSpotStatus.OCCUPIED);
        }

        @Test
        @DisplayName("returns AVAILABLE when release covers now but no booking")
        void available() {
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(
                    1L, spot, NOW.minusHours(1), NOW.plusHours(5));

            when(releaseRepository.findByParkingSpotSpotNumber(53)).thenReturn(List.of(release));

            assertThat(service.computeStatus(spot, NOW)).isEqualTo(ParkingSpotStatus.AVAILABLE);
        }

        @Test
        @DisplayName("returns BOOKED when release and booking cover now")
        void booked() {
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(
                    1L, spot, NOW.minusHours(1), NOW.plusHours(5));
            final ParkingSpotBookingEntity booking = TestEntityFactory.createBooking(
                    1L, release, seeker, NOW.minusMinutes(30), NOW.plusHours(2));
            release.getBookings().add(booking);

            when(releaseRepository.findByParkingSpotSpotNumber(53)).thenReturn(List.of(release));

            assertThat(service.computeStatus(spot, NOW)).isEqualTo(ParkingSpotStatus.BOOKED);
        }

        @Test
        @DisplayName("returns OCCUPIED when release exists but does not cover now")
        void releaseNotCoveringNow() {
            final ParkingSpotReleaseEntity futureRelease = TestEntityFactory.createRelease(
                    1L, spot, NOW.plusHours(5), NOW.plusHours(10));

            when(releaseRepository.findByParkingSpotSpotNumber(53)).thenReturn(List.of(futureRelease));

            assertThat(service.computeStatus(spot, NOW)).isEqualTo(ParkingSpotStatus.OCCUPIED);
        }

        @Test
        @DisplayName("returns AVAILABLE when booking exists but does not cover now")
        void bookingNotCoveringNow() {
            final ParkingSpotReleaseEntity release = TestEntityFactory.createRelease(
                    1L, spot, NOW.minusHours(2), NOW.plusHours(10));
            final ParkingSpotBookingEntity booking = TestEntityFactory.createBooking(
                    1L, release, seeker, NOW.plusHours(3), NOW.plusHours(5));
            release.getBookings().add(booking);

            when(releaseRepository.findByParkingSpotSpotNumber(53)).thenReturn(List.of(release));

            assertThat(service.computeStatus(spot, NOW)).isEqualTo(ParkingSpotStatus.AVAILABLE);
        }
    }

    // ===================== Report Management =====================

    @Nested
    @DisplayName("Reports")
    class Reports {

        @Test
        @DisplayName("creates report successfully")
        void createReport_success() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ParkingSpotReportEntity result = service.createReport(53, 2L, "Spot is blocked");

            assertThat(result.getComment()).isEqualTo("Spot is blocked");
            assertThat(result.getStatus()).isEqualTo(ReportStatus.OPEN);
        }

        @Test
        @DisplayName("creates report with null comment")
        void createReport_nullComment() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            when(userRepository.findById(2L)).thenReturn(Optional.of(seeker));
            when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final ParkingSpotReportEntity result = service.createReport(53, 2L, null);

            assertThat(result.getComment()).isNull();
        }

        @Test
        @DisplayName("throws NoSuchElementException when spot not found")
        void createReport_spotNotFound() {
            when(parkingSpotRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createReport(999, 2L, "test"))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("throws NoSuchElementException when reporter not found")
        void createReport_reporterNotFound() {
            when(parkingSpotRepository.findById(53)).thenReturn(Optional.of(spot));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createReport(53, 999L, "test"))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("updates report status")
        void updateReportStatus_success() {
            final ParkingSpotReportEntity report = TestEntityFactory.createReport(1L, spot, seeker, "issue");
            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

            service.updateReportStatus(1L, ReportStatus.RESOLVED);

            assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
            verify(reportRepository).save(report);
        }

        @Test
        @DisplayName("throws NoSuchElementException when report not found")
        void updateReportStatus_notFound() {
            when(reportRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateReportStatus(999L, ReportStatus.DISMISSED))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getAllReports returns ordered list")
        void getAllReports() {
            final ParkingSpotReportEntity r1 = TestEntityFactory.createReport(1L, spot, seeker, "first");
            final ParkingSpotReportEntity r2 = TestEntityFactory.createReport(2L, spot, seeker, "second");
            when(reportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(r2, r1));

            final List<ParkingSpotReportEntity> result = service.getAllReports();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getComment()).isEqualTo("second");
        }
    }
}
