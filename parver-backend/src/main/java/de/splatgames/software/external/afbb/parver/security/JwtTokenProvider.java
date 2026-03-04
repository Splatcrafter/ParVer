package de.splatgames.software.external.afbb.parver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtTokenProvider(
            @NotNull @Value("${parver.security.jwt.secret}") final String secret,
            @Value("${parver.security.jwt.access-token-expiration:900}") final long accessTokenExpirationSeconds,
            @Value("${parver.security.jwt.refresh-token-expiration:604800}") final long refreshTokenExpirationSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    @NotNull
    public String generateAccessToken(@NotNull final String username, @NotNull final String role) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(this.accessTokenExpirationSeconds)))
                .signWith(this.signingKey)
                .compact();
    }

    @NotNull
    public String generateRefreshToken(@NotNull final String username) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(this.refreshTokenExpirationSeconds)))
                .signWith(this.signingKey)
                .compact();
    }

    public long getAccessTokenExpirationSeconds() {
        return this.accessTokenExpirationSeconds;
    }

    @Nullable
    public String extractUsername(@NotNull final String token) {
        final Claims claims = parseClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    @Nullable
    public String extractRole(@NotNull final String token) {
        final Claims claims = parseClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    public boolean isRefreshToken(@NotNull final String token) {
        final Claims claims = parseClaims(token);
        return claims != null && "refresh".equals(claims.get("type", String.class));
    }

    public boolean isTokenValid(@NotNull final String token) {
        return parseClaims(token) != null;
    }

    @Nullable
    private Claims parseClaims(@NotNull final String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (final JwtException | IllegalArgumentException e) {
            LOG.debug("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }
}
