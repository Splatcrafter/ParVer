package de.splatgames.software.external.afbb.parver.setup;

import org.jetbrains.annotations.NotNull;

public interface SetupService {

    @NotNull
    String generateSetupToken();

    boolean validateSetupToken(@NotNull String token);
}
