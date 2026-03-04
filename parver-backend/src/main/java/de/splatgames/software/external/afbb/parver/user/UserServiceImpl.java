package de.splatgames.software.external.afbb.parver.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            @NotNull final UserRepository userRepository,
            @NotNull final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @NotNull
    public UserEntity createUser(
            @NotNull final String username,
            @NotNull final String displayName,
            @NotNull final String rawPassword,
            @NotNull final UserRole role) {

        if (this.userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        final String hash = this.passwordEncoder.encode(rawPassword);
        final var entity = new UserEntity(username, displayName, hash, role);
        return this.userRepository.save(entity);
    }

    @Override
    @NotNull
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsername(@NotNull final String username) {
        return this.userRepository.findByUsername(username);
    }

    @Override
    @NotNull
    @Transactional(readOnly = true)
    public Optional<UserEntity> findById(final long id) {
        return this.userRepository.findById(id);
    }

    @Override
    @NotNull
    @Transactional(readOnly = true)
    public List<UserEntity> findAll() {
        return this.userRepository.findAll();
    }

    @Override
    @NotNull
    public UserEntity updateUser(
            final long id,
            @NotNull final String displayName,
            @Nullable final String rawPassword,
            @NotNull final UserRole role) {

        final UserEntity entity = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));

        entity.setDisplayName(displayName);
        entity.setRole(role);

        if (rawPassword != null && !rawPassword.isBlank()) {
            entity.setPasswordHash(this.passwordEncoder.encode(rawPassword));
        }

        return this.userRepository.save(entity);
    }

    @Override
    public void deleteUser(final long id) {
        if (!this.userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found: " + id);
        }
        this.userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean adminExists() {
        return this.userRepository.existsByRole(UserRole.ADMIN);
    }
}
