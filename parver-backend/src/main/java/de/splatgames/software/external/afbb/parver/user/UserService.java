package de.splatgames.software.external.afbb.parver.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface UserService {

    @NotNull
    UserEntity createUser(
            @NotNull String username,
            @NotNull String displayName,
            @NotNull String rawPassword,
            @NotNull UserRole role);

    @NotNull
    Optional<UserEntity> findByUsername(@NotNull String username);

    @NotNull
    Optional<UserEntity> findById(long id);

    @NotNull
    List<UserEntity> findAll();

    @NotNull
    UserEntity updateUser(
            long id,
            @NotNull String displayName,
            @Nullable String rawPassword,
            @NotNull UserRole role);

    void deleteUser(long id);

    boolean adminExists();
}
