package de.splatgames.software.external.afbb.parver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long-for-hmac-sha256";

    private JwtTokenProvider tokenProvider;
    private JwtAuthenticationFilter filter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, 900, 604800);
        filter = new JwtAuthenticationFilter(tokenProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("sets authentication from valid Bearer token")
    void validBearerToken() throws ServletException, IOException {
        final String token = tokenProvider.generateAccessToken("admin", "ADMIN");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("admin");
        assertThat(auth.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("sets authentication from query parameter token (SSE support)")
    void tokenFromQueryParam() throws ServletException, IOException {
        final String token = tokenProvider.generateAccessToken("user1", "USER");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getParameter("token")).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("user1");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("proceeds without authentication when no token provided")
    void noToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getParameter("token")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("proceeds without authentication for invalid token")
    void invalidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.jwt.token");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("rejects refresh token as access token")
    void rejectsRefreshToken() throws ServletException, IOException {
        final String refreshToken = tokenProvider.generateRefreshToken("user1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + refreshToken);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("sets correct ROLE_ prefix authority")
    void correctAuthorityPrefix() throws ServletException, IOException {
        final String token = tokenProvider.generateAccessToken("user1", "USER");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("proceeds without authentication for expired token")
    void expiredToken() throws ServletException, IOException {
        final var expiredProvider = new JwtTokenProvider(SECRET, 0, 0);
        final String token = expiredProvider.generateAccessToken("user", "USER");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
