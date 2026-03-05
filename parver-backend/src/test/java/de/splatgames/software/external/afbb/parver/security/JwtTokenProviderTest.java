package de.splatgames.software.external.afbb.parver.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long-for-hmac-sha256";

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, 900, 604800);
    }

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateAccessToken {

        @Test
        @DisplayName("generates a non-empty token")
        void generatesToken() {
            final String token = tokenProvider.generateAccessToken("admin", "ADMIN");

            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("token contains correct username")
        void containsUsername() {
            final String token = tokenProvider.generateAccessToken("johndoe", "USER");

            assertThat(tokenProvider.extractUsername(token)).isEqualTo("johndoe");
        }

        @Test
        @DisplayName("token contains correct role")
        void containsRole() {
            final String token = tokenProvider.generateAccessToken("admin", "ADMIN");

            assertThat(tokenProvider.extractRole(token)).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("access token is not a refresh token")
        void notRefreshToken() {
            final String token = tokenProvider.generateAccessToken("user", "USER");

            assertThat(tokenProvider.isRefreshToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("generateRefreshToken")
    class GenerateRefreshToken {

        @Test
        @DisplayName("generates a non-empty token")
        void generatesToken() {
            final String token = tokenProvider.generateRefreshToken("user");

            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("contains correct username")
        void containsUsername() {
            final String token = tokenProvider.generateRefreshToken("johndoe");

            assertThat(tokenProvider.extractUsername(token)).isEqualTo("johndoe");
        }

        @Test
        @DisplayName("is identified as refresh token")
        void isRefreshToken() {
            final String token = tokenProvider.generateRefreshToken("user");

            assertThat(tokenProvider.isRefreshToken(token)).isTrue();
        }

        @Test
        @DisplayName("has no role claim")
        void noRoleClaim() {
            final String token = tokenProvider.generateRefreshToken("user");

            assertThat(tokenProvider.extractRole(token)).isNull();
        }
    }

    @Nested
    @DisplayName("extractUsername")
    class ExtractUsername {

        @Test
        @DisplayName("returns null for invalid token")
        void invalidToken() {
            assertThat(tokenProvider.extractUsername("not.a.jwt")).isNull();
        }

        @Test
        @DisplayName("returns null for random string")
        void randomString() {
            assertThat(tokenProvider.extractUsername("foobar123")).isNull();
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("returns true for valid access token")
        void validAccessToken() {
            final String token = tokenProvider.generateAccessToken("user", "USER");

            assertThat(tokenProvider.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("returns true for valid refresh token")
        void validRefreshToken() {
            final String token = tokenProvider.generateRefreshToken("user");

            assertThat(tokenProvider.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("returns false for tampered token")
        void tamperedToken() {
            final String token = tokenProvider.generateAccessToken("user", "USER");
            final String tampered = token.substring(0, token.length() - 5) + "XXXXX";

            assertThat(tokenProvider.isTokenValid(tampered)).isFalse();
        }

        @Test
        @DisplayName("returns false for random string")
        void randomString() {
            assertThat(tokenProvider.isTokenValid("totally-not-a-jwt")).isFalse();
        }

        @Test
        @DisplayName("returns false for empty string")
        void emptyString() {
            assertThat(tokenProvider.isTokenValid("")).isFalse();
        }

        @Test
        @DisplayName("returns false for token signed with different secret")
        void differentSecret() {
            final var otherProvider = new JwtTokenProvider(
                    "another-secret-key-that-is-at-least-32-bytes-for-hmac-sha256", 900, 604800);
            final String token = otherProvider.generateAccessToken("user", "USER");

            assertThat(tokenProvider.isTokenValid(token)).isFalse();
        }

        @Test
        @DisplayName("returns false for expired token")
        void expiredToken() {
            // Create a provider with 0 second expiration
            final var shortLivedProvider = new JwtTokenProvider(SECRET, 0, 0);
            final String token = shortLivedProvider.generateAccessToken("user", "USER");

            assertThat(tokenProvider.isTokenValid(token)).isFalse();
        }
    }

    @Test
    @DisplayName("getAccessTokenExpirationSeconds returns configured value")
    void getAccessTokenExpirationSeconds() {
        assertThat(tokenProvider.getAccessTokenExpirationSeconds()).isEqualTo(900);
    }
}
