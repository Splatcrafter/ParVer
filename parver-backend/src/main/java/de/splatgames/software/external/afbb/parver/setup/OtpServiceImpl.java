package de.splatgames.software.external.afbb.parver.setup;

import de.splatgames.software.external.afbb.parver.user.UserService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger LOG = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final int OTP_LENGTH = 6;

    private final UserService userService;
    private final SecureRandom secureRandom = new SecureRandom();

    private volatile String currentOtp;

    public OtpServiceImpl(@NotNull final UserService userService) {
        this.userService = userService;
        rotateOtp();
    }

    @Scheduled(fixedRate = 300_000)
    public void rotateOtp() {
        if (this.userService.adminExists()) {
            this.currentOtp = null;
            return;
        }

        this.currentOtp = generateOtp();
        LOG.info("===========================================");
        LOG.info("  SETUP OTP: {}", this.currentOtp);
        LOG.info("  Valid for 5 minutes");
        LOG.info("===========================================");
    }

    @Override
    public boolean verify(@NotNull final String otp) {
        return this.currentOtp != null && this.currentOtp.equals(otp);
    }

    @NotNull
    private String generateOtp() {
        final StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(this.secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
