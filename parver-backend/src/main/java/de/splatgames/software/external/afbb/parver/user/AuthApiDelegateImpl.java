package de.splatgames.software.external.afbb.parver.user;

import de.splatgames.software.external.afbb.parver.api.AuthApiDelegate;
import de.splatgames.software.external.afbb.parver.model.LoginRequest;
import de.splatgames.software.external.afbb.parver.model.LoginResponse;
import de.splatgames.software.external.afbb.parver.model.RefreshTokenRequest;
import de.splatgames.software.external.afbb.parver.model.UserResponse;
import de.splatgames.software.external.afbb.parver.security.JwtTokenProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthApiDelegateImpl implements AuthApiDelegate {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthApiDelegateImpl(
            @NotNull final AuthenticationManager authenticationManager,
            @NotNull final JwtTokenProvider jwtTokenProvider,
            @NotNull final UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<LoginResponse> login(@NotNull final LoginRequest loginRequest) {
        try {
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
        } catch (final BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }

        final UserEntity user = this.userService.findByUsername(loginRequest.getUsername())
                .orElseThrow();

        return ResponseEntity.ok(buildLoginResponse(user));
    }

    @Override
    public ResponseEntity<LoginResponse> refreshToken(@NotNull final RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();

        if (!this.jwtTokenProvider.isTokenValid(refreshToken)
                || !this.jwtTokenProvider.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        final String username = this.jwtTokenProvider.extractUsername(refreshToken);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }

        final UserEntity user = this.userService.findByUsername(username)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(buildLoginResponse(user));
    }

    @Override
    public ResponseEntity<UserResponse> getCurrentUser() {
        final String username = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        final UserEntity user = this.userService.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @NotNull
    private LoginResponse buildLoginResponse(@NotNull final UserEntity user) {
        final String accessToken = this.jwtTokenProvider.generateAccessToken(
                user.getUsername(), user.getRole().name());
        final String refreshToken = this.jwtTokenProvider.generateRefreshToken(
                user.getUsername());

        return new LoginResponse()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(this.jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(UserMapper.toResponse(user));
    }
}
