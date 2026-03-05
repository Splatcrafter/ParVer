package de.splatgames.software.external.afbb.parver.setup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SetupServiceImpl")
class SetupServiceImplTest {

    private SetupServiceImpl setupService;

    @BeforeEach
    void setUp() {
        setupService = new SetupServiceImpl();
    }

    @Test
    @DisplayName("generateSetupToken returns non-null UUID string")
    void generateSetupToken_returnsUUID() {
        final String token = setupService.generateSetupToken();

        assertThat(token).isNotNull().isNotBlank();
        // UUID format: 8-4-4-4-12
        assertThat(token).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("validateSetupToken returns true for valid token within TTL")
    void validateSetupToken_valid() {
        final String token = setupService.generateSetupToken();

        assertThat(setupService.validateSetupToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateSetupToken returns false for wrong token")
    void validateSetupToken_wrongToken() {
        setupService.generateSetupToken();

        assertThat(setupService.validateSetupToken("wrong-token")).isFalse();
    }

    @Test
    @DisplayName("validateSetupToken returns false for expired token")
    void validateSetupToken_expired() {
        setupService.generateSetupToken();

        // Force the expiry to the past
        ReflectionTestUtils.setField(setupService, "setupTokenExpiry", Instant.now().minusSeconds(1));

        final String token = (String) ReflectionTestUtils.getField(setupService, "currentSetupToken");
        assertThat(setupService.validateSetupToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateSetupToken returns false when no token was generated")
    void validateSetupToken_noGeneration() {
        assertThat(setupService.validateSetupToken("some-token")).isFalse();
    }

    @Test
    @DisplayName("generating new token invalidates old token")
    void newTokenInvalidatesOld() {
        final String firstToken = setupService.generateSetupToken();
        final String secondToken = setupService.generateSetupToken();

        assertThat(setupService.validateSetupToken(firstToken)).isFalse();
        assertThat(setupService.validateSetupToken(secondToken)).isTrue();
    }
}
