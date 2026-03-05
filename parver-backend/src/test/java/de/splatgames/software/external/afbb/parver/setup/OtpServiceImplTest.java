package de.splatgames.software.external.afbb.parver.setup;

import de.splatgames.software.external.afbb.parver.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpServiceImpl")
class OtpServiceImplTest {

    @Test
    @DisplayName("verify returns true for correct OTP")
    void verifyCorrectOtp() {
        final UserService userService = mock(UserService.class);
        when(userService.adminExists()).thenReturn(false);
        final OtpServiceImpl otpService = new OtpServiceImpl(userService);

        // Get the generated OTP via reflection
        final String otp = (String) ReflectionTestUtils.getField(otpService, "currentOtp");
        assertThat(otp).isNotNull();

        assertThat(otpService.verify(otp)).isTrue();
    }

    @Test
    @DisplayName("verify returns false for incorrect OTP")
    void verifyIncorrectOtp() {
        final UserService userService = mock(UserService.class);
        when(userService.adminExists()).thenReturn(false);
        final OtpServiceImpl otpService = new OtpServiceImpl(userService);

        assertThat(otpService.verify("000000")).isFalse();
    }

    @Test
    @DisplayName("verify returns false when OTP is null (admin exists)")
    void verifyWhenOtpNull() {
        final UserService userService = mock(UserService.class);
        when(userService.adminExists()).thenReturn(true);
        final OtpServiceImpl otpService = new OtpServiceImpl(userService);

        assertThat(otpService.verify("123456")).isFalse();
    }

    @Test
    @DisplayName("rotateOtp generates new OTP when no admin exists")
    void rotateOtpGeneratesNew() {
        final UserService userService = mock(UserService.class);
        when(userService.adminExists()).thenReturn(false);
        final OtpServiceImpl otpService = new OtpServiceImpl(userService);

        final String firstOtp = (String) ReflectionTestUtils.getField(otpService, "currentOtp");
        // Rotate multiple times to increase chance of different OTP (6 digits = very high chance)
        boolean changed = false;
        for (int i = 0; i < 10; i++) {
            otpService.rotateOtp();
            final String newOtp = (String) ReflectionTestUtils.getField(otpService, "currentOtp");
            if (!firstOtp.equals(newOtp)) {
                changed = true;
                break;
            }
        }
        assertThat(changed).isTrue();
    }

    @Test
    @DisplayName("rotateOtp sets OTP to null when admin exists")
    void rotateOtpAdminExists() {
        final UserService userService = mock(UserService.class);
        when(userService.adminExists()).thenReturn(false);
        final OtpServiceImpl otpService = new OtpServiceImpl(userService);

        // Verify OTP is set initially
        assertThat(ReflectionTestUtils.getField(otpService, "currentOtp")).isNotNull();

        // Simulate admin creation
        when(userService.adminExists()).thenReturn(true);
        otpService.rotateOtp();

        assertThat(ReflectionTestUtils.getField(otpService, "currentOtp")).isNull();
    }

    @Test
    @DisplayName("generated OTP is exactly 6 digits")
    void otpIs6Digits() {
        final UserService userService = mock(UserService.class);
        when(userService.adminExists()).thenReturn(false);
        final OtpServiceImpl otpService = new OtpServiceImpl(userService);

        final String otp = (String) ReflectionTestUtils.getField(otpService, "currentOtp");
        assertThat(otp).hasSize(6);
        assertThat(otp).matches("\\d{6}");
    }
}
