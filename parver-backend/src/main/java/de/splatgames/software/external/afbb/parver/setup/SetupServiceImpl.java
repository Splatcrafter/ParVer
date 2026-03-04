package de.splatgames.software.external.afbb.parver.setup;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SetupServiceImpl implements SetupService {

    private static final long SETUP_TOKEN_TTL_SECONDS = 600;

    private volatile String currentSetupToken;
    private volatile Instant setupTokenExpiry;

    @Override
    @NotNull
    public String generateSetupToken() {
        this.currentSetupToken = UUID.randomUUID().toString();
        this.setupTokenExpiry = Instant.now().plusSeconds(SETUP_TOKEN_TTL_SECONDS);
        return this.currentSetupToken;
    }

    @Override
    public boolean validateSetupToken(@NotNull final String token) {
        return this.currentSetupToken != null
                && this.currentSetupToken.equals(token)
                && Instant.now().isBefore(this.setupTokenExpiry);
    }
}
