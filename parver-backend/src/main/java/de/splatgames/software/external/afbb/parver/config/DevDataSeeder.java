package de.splatgames.software.external.afbb.parver.config;

import de.splatgames.software.external.afbb.parver.parking.ParkingSpotService;
import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserRole;
import de.splatgames.software.external.afbb.parver.user.UserService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;

@Configuration
@Profile("dev")
public class DevDataSeeder {

    private static final Logger LOG = LoggerFactory.getLogger(DevDataSeeder.class);

    @Bean
    @Order(2)
    @DependsOn("seedParkingSpots")
    public CommandLineRunner seedDevData(
            @NotNull final UserService userService,
            @NotNull final ParkingSpotService parkingSpotService) {
        return args -> {
            // Create users
            final UserEntity admin = userService.createUser(
                    "admin", "Administrator", "admin123!", UserRole.ADMIN);
            final UserEntity lehrer1 = userService.createUser(
                    "lehrer1", "Max Mustermann", "password1!", UserRole.USER);
            final UserEntity lehrer2 = userService.createUser(
                    "lehrer2", "Erika Musterfrau", "password2!", UserRole.USER);
            final UserEntity suchender = userService.createUser(
                    "suchender", "Hans Suchend", "password3!", UserRole.USER);

            // Assign some spots to users
            parkingSpotService.assignOwner(53, lehrer1.getId());
            parkingSpotService.assignOwner(57, lehrer2.getId());

            // Create a test release: lehrer1 releases spot 53 for the next 7 days
            final LocalDateTime now = LocalDateTime.now();
            parkingSpotService.createRelease(53, lehrer1.getId(),
                    now.plusHours(1), now.plusDays(7));

            // Create a test booking: suchender books spot 53 for tomorrow
            final LocalDateTime tomorrowStart = now.plusDays(1).withHour(8).withMinute(0);
            final LocalDateTime tomorrowEnd = now.plusDays(1).withHour(17).withMinute(0);
            // Only book if the booking falls within the release window
            if (!tomorrowStart.isBefore(now.plusHours(1))) {
                parkingSpotService.createBooking(53, 1L, suchender.getId(),
                        tomorrowStart, tomorrowEnd);
            }

            LOG.info("Dev data seeded: 4 users, 2 assigned, 1 release, 1 booking");
        };
    }
}
