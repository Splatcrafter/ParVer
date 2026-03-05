package de.splatgames.software.external.afbb.parver.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity("testuser", "Test User", "hashed", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    // --- createUser ---

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("creates user with encoded password")
        void success() {
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
            when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            final UserEntity result = userService.createUser("newuser", "New User",
                    "raw-password", UserRole.USER);

            assertThat(result.getUsername()).isEqualTo("newuser");
            assertThat(result.getDisplayName()).isEqualTo("New User");
            assertThat(result.getPasswordHash()).isEqualTo("encoded-password");
            assertThat(result.getRole()).isEqualTo(UserRole.USER);
            verify(passwordEncoder).encode("raw-password");
        }

        @Test
        @DisplayName("creates admin user")
        void createsAdmin() {
            when(userRepository.existsByUsername("admin")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final UserEntity result = userService.createUser("admin", "Admin", "pass", UserRole.ADMIN);

            assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for duplicate username")
        void duplicateUsername() {
            when(userRepository.existsByUsername("existing")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser("existing", "Name", "pass", UserRole.USER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            verify(userRepository, never()).save(any());
        }
    }

    // --- findByUsername ---

    @Nested
    @DisplayName("findByUsername")
    class FindByUsername {

        @Test
        @DisplayName("returns user when found")
        void exists() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            final Optional<UserEntity> result = userService.findByUsername("testuser");

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("returns empty when not found")
        void notExists() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThat(userService.findByUsername("ghost")).isEmpty();
        }
    }

    // --- findById ---

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns user when found")
        void exists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            assertThat(userService.findById(1L)).isPresent();
        }

        @Test
        @DisplayName("returns empty when not found")
        void notExists() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThat(userService.findById(999L)).isEmpty();
        }
    }

    // --- findAll ---

    @Test
    @DisplayName("findAll returns all users")
    void findAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        assertThat(userService.findAll()).hasSize(1);
    }

    // --- updateUser ---

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("updates display name, role, and password")
        void updatesAllFields() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newpass")).thenReturn("new-hashed");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            final UserEntity result = userService.updateUser(1L, "Updated Name", "newpass", UserRole.ADMIN);

            assertThat(result.getDisplayName()).isEqualTo("Updated Name");
            assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(result.getPasswordHash()).isEqualTo("new-hashed");
        }

        @Test
        @DisplayName("skips password encoding when password is null")
        void skipsNullPassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUser(1L, "Name", null, UserRole.USER);

            verify(passwordEncoder, never()).encode(any());
            assertThat(testUser.getPasswordHash()).isEqualTo("hashed");
        }

        @Test
        @DisplayName("skips password encoding when password is blank")
        void skipsBlankPassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUser(1L, "Name", "   ", UserRole.USER);

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("skips password encoding when password is empty string")
        void skipsEmptyPassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUser(1L, "Name", "", UserRole.USER);

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("throws NoSuchElementException when user not found")
        void notFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(999L, "X", "pass", UserRole.USER))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // --- deleteUser ---

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("deletes existing user")
        void success() {
            when(userRepository.existsById(1L)).thenReturn(true);

            userService.deleteUser(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws NoSuchElementException when user not found")
        void notFound() {
            when(userRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // --- adminExists ---

    @Nested
    @DisplayName("adminExists")
    class AdminExists {

        @Test
        @DisplayName("returns true when admin exists")
        void adminPresent() {
            when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);

            assertThat(userService.adminExists()).isTrue();
        }

        @Test
        @DisplayName("returns false when no admin exists")
        void noAdmin() {
            when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

            assertThat(userService.adminExists()).isFalse();
        }
    }
}
