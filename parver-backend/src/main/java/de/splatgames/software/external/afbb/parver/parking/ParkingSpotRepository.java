package de.splatgames.software.external.afbb.parver.parking;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpotEntity, Integer> {

    @NotNull
    Optional<ParkingSpotEntity> findByOwnerId(@NotNull Long ownerId);
}
