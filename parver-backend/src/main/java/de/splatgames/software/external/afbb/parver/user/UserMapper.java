package de.splatgames.software.external.afbb.parver.user;

import de.splatgames.software.external.afbb.parver.model.UserResponse;
import org.jetbrains.annotations.NotNull;

public final class UserMapper {

    private UserMapper() {
        // Utility class
    }

    @NotNull
    public static UserResponse toResponse(@NotNull final UserEntity entity) {
        return new UserResponse()
                .id(entity.getId())
                .username(entity.getUsername())
                .displayName(entity.getDisplayName())
                .role(UserResponse.RoleEnum.valueOf(entity.getRole().name()))
                .parkingSpotNumber(
                        entity.getParkingSpot() != null
                                ? entity.getParkingSpot().getSpotNumber()
                                : null);
    }
}
