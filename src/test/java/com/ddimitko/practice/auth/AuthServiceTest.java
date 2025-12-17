package com.ddimitko.practice.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddimitko.practice.auth.dto.AuthResponse;
import com.ddimitko.practice.user.AppUser;
import com.ddimitko.practice.user.AppUserRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private OtpService otpService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(appUserRepository, authTokenRepository, otpService);
    }

    @Test
    void verifyAndLogin_createsNewUserAndIssuesToken() throws Exception {
        String email = "user@example.com";
        when(otpService.verifyOtp(email, "111111")).thenReturn(true);
        when(appUserRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            setId(user, 1L);
            return user;
        });
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.verifyAndLogin(email, "111111");

        verify(authTokenRepository).revokeAllForUser(1L);
        assertNotNull(response.token());
        assertEquals(email, response.email());
        assertEquals(true, response.needsProfile());
        assertEquals(true, response.newUser());
    }

    @Test
    void verifyAndLogin_rejectsInvalidOtp() {
        when(otpService.verifyOtp("user@example.com", "bad")).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> authService.verifyAndLogin("user@example.com", "bad"));
    }

    private void setId(AppUser user, Long id) throws Exception {
        Field idField = AppUser.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }
}
