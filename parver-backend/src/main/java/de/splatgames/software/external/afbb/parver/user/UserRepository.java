package de.splatgames.software.external.afbb.parver.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @NotNull
    Optional<UserEntity> findByUsername(@NotNull String username);

    boolean existsByUsername(@NotNull String username);

    boolean existsByRole(@NotNull UserRole role);
}
