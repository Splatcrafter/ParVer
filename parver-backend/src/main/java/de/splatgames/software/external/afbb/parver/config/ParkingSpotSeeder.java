package de.splatgames.software.external.afbb.parver.config;

import de.splatgames.software.external.afbb.parver.parking.ParkingSpotRepository;
import de.splatgames.software.external.afbb.parver.parking.ParkingSpotService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ParkingSpotSeeder {

    private static final Logger LOG = LoggerFactory.getLogger(ParkingSpotSeeder.class);

    @Bean
    @Order(1)
    public CommandLineRunner seedParkingSpots(
            @NotNull final ParkingSpotService parkingSpotService,
            @NotNull final ParkingSpotRepository parkingSpotRepository) {
        return args -> {
            if (parkingSpotRepository.count() > 0) {
                LOG.info("Parking spots already exist, skipping seeding");
                return;
            }

            // Create parking spots for small area (53-61)
            for (int i = 53; i <= 61; i++) {
                parkingSpotService.createSpot(i, "small");
            }

            // Create parking spots for large area (1-30)
            for (int i = 1; i <= 30; i++) {
                parkingSpotService.createSpot(i, "large");
            }

            LOG.info("Parking spots seeded: 9 small spots (53-61), 30 large spots (1-30)");
        };
    }
}
