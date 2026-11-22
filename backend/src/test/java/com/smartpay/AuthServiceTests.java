package com.smartpay;

import com.smartpay.auth.dto.AuthRequest;
import com.smartpay.auth.dto.AuthResponse;
import com.smartpay.auth.dto.RegisterRequest;
import com.smartpay.auth.service.AuthService;
import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Should successfully register a new user and return JWT tokens")
    void testRegisterUser() {
        RegisterRequest request = new RegisterRequest(
                "sarah.connor@smartpay.internal",
                "SecurePass123!",
                "Sarah",
                "Connor"
        );

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("sarah.connor@smartpay.internal", response.user().email());
    }

    @Test
    @DisplayName("Should reject duplicate user registration with Conflict")
    void testDuplicateRegistration() {
        RegisterRequest request = new RegisterRequest(
                "duplicate@smartpay.internal",
                "SecurePass123!",
                "Test",
                "User"
        );

        authService.register(request);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should authenticate existing user with valid password and reject invalid password")
    void testLogin() {
        RegisterRequest registerReq = new RegisterRequest(
                "john.doe@smartpay.internal",
                "CorrectPassword123!",
                "John",
                "Doe"
        );
        authService.register(registerReq);

        // Valid login
        AuthResponse loginRes = authService.login(new AuthRequest("john.doe@smartpay.internal", "CorrectPassword123!"));
        assertNotNull(loginRes);
        assertNotNull(loginRes.accessToken());

        // Invalid password
        assertThrows(BusinessException.class, () ->
                authService.login(new AuthRequest("john.doe@smartpay.internal", "WrongPassword!"))
        );
    }
}
