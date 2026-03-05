package de.splatgames.software.external.afbb.parver.security;

import de.splatgames.software.external.afbb.parver.TestEntityFactory;
import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserRepository;
import de.splatgames.software.external.afbb.parver.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("returns UserDetails for existing user")
    void existingUser() {
        final UserEntity user = TestEntityFactory.createUser(1L, "admin", "Admin", UserRole.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        final UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getPassword()).isEqualTo("hashed-password");
    }

    @Test
    @DisplayName("throws UsernameNotFoundException for non-existent user")
    void nonExistent() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    @DisplayName("sets correct ROLE_ authority format")
    void correctAuthorityFormat() {
        final UserEntity user = TestEntityFactory.createUser(1L, "user1", "User", UserRole.USER);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        final UserDetails result = userDetailsService.loadUserByUsername("user1");

        assertThat(result.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("sets correct ROLE_ADMIN authority for admin user")
    void adminAuthority() {
        final UserEntity user = TestEntityFactory.createUser(1L, "admin", "Admin", UserRole.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        final UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(result.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_ADMIN");
    }
}
