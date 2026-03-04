package de.splatgames.software.external.afbb.parver.setup;

import org.jetbrains.annotations.NotNull;

public interface OtpService {

    boolean verify(@NotNull String otp);
}
