package de.splatgames.software.external.afbb.parver.setup;

import de.splatgames.software.external.afbb.parver.api.SetupApiDelegate;
import de.splatgames.software.external.afbb.parver.model.CreateAdminRequest;
import de.splatgames.software.external.afbb.parver.model.OtpVerificationRequest;
import de.splatgames.software.external.afbb.parver.model.OtpVerificationResponse;
import de.splatgames.software.external.afbb.parver.model.SetupStatusResponse;
import de.splatgames.software.external.afbb.parver.model.UserResponse;
import de.splatgames.software.external.afbb.parver.user.UserEntity;
import de.splatgames.software.external.afbb.parver.user.UserMapper;
import de.splatgames.software.external.afbb.parver.user.UserRole;
import de.splatgames.software.external.afbb.parver.user.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SetupApiDelegateImpl implements SetupApiDelegate {

    private final SetupService setupService;
    private final OtpService otpService;
    private final UserService userService;

    public SetupApiDelegateImpl(
            @NotNull final SetupService setupService,
            @NotNull final OtpService otpService,
            @NotNull final UserService userService) {
        this.setupService = setupService;
        this.otpService = otpService;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<SetupStatusResponse> getSetupStatus() {
        final boolean setupRequired = !this.userService.adminExists();
        final var currentStep = setupRequired
                ? SetupStatusResponse.CurrentStepEnum.OTP_VERIFICATION
                : SetupStatusResponse.CurrentStepEnum.COMPLETED;

        return ResponseEntity.ok(new SetupStatusResponse()
                .setupRequired(setupRequired)
                .currentStep(currentStep));
    }

    @Override
    public ResponseEntity<OtpVerificationResponse> verifySetupOtp(
            @NotNull final OtpVerificationRequest request) {

        if (this.userService.adminExists()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (!this.otpService.verify(request.getOtp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final String setupToken = this.setupService.generateSetupToken();
        return ResponseEntity.ok(new OtpVerificationResponse().setupToken(setupToken));
    }

    @Override
    public ResponseEntity<UserResponse> createInitialAdmin(
            @NotNull final CreateAdminRequest request) {

        if (this.userService.adminExists()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (!this.setupService.validateSetupToken(request.getSetupToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final UserEntity admin = this.userService.createUser(
                request.getUsername(),
                request.getDisplayName(),
                request.getPassword(),
                UserRole.ADMIN);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserMapper.toResponse(admin));
    }
}
