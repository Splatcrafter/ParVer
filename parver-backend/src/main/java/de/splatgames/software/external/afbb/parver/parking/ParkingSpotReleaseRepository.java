package de.splatgames.software.external.afbb.parver.parking;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParkingSpotReleaseRepository extends JpaRepository<ParkingSpotReleaseEntity, Long> {

    @NotNull
    List<ParkingSpotReleaseEntity> findByParkingSpotSpotNumber(int spotNumber);

    @Query("SELECT r FROM ParkingSpotReleaseEntity r WHERE r.parkingSpot.spotNumber = :spotNumber " +
            "AND r.availableFrom < :to AND r.availableTo > :from")
    @NotNull
    List<ParkingSpotReleaseEntity> findOverlapping(@Param("spotNumber") int spotNumber,
                                                    @NotNull @Param("from") LocalDateTime from,
                                                    @NotNull @Param("to") LocalDateTime to);

    @NotNull
    List<ParkingSpotReleaseEntity> findByParkingSpotSpotNumberAndAvailableToAfter(
            int spotNumber, @NotNull LocalDateTime after);
}
