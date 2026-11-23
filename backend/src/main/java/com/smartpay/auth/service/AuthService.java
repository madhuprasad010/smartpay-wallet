package com.smartpay.auth.service;

import com.smartpay.audit.dto.CreateAuditEventRequest;
import com.smartpay.audit.service.AuditService;
import com.smartpay.auth.dto.*;
import com.smartpay.auth.entity.*;
import com.smartpay.auth.repository.RefreshTokenRepository;
import com.smartpay.auth.repository.UserRepository;
import com.smartpay.common.exception.BusinessException;
import com.smartpay.common.exception.DuplicateResourceException;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.config.KafkaEventPublisher;
import com.smartpay.security.JwtTokenProvider;
import com.smartpay.user.entity.UserProfile;
import com.smartpay.user.repository.UserProfileRepository;
import com.smartpay.wallet.entity.Currency;
import com.smartpay.wallet.entity.Wallet;
import com.smartpay.wallet.entity.WalletLimit;
import com.smartpay.wallet.entity.WalletStatus;
import com.smartpay.wallet.repository.WalletLimitRepository;
import com.smartpay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserProfileRepository userProfileRepository;
    private final WalletRepository walletRepository;
    private final WalletLimitRepository walletLimitRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final KafkaEventPublisher kafkaPublisher;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User with email already exists: " + request.email());
        }

        User user = User.builder()
                .email(request.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        // Auto-provision UserProfile
        UserProfile profile = UserProfile.builder()
                .userId(savedUser.getId())
                .firstName(request.firstName() != null ? request.firstName().trim() : "")
                .lastName(request.lastName() != null ? request.lastName().trim() : "")
                .kycStatus("UNVERIFIED")
                .build();
        userProfileRepository.save(profile);

        // Auto-provision initial INR Wallet
        String walletNumber = "WLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Wallet wallet = Wallet.builder()
                .userId(savedUser.getId())
                .walletNumber(walletNumber)
                .currency(Currency.INR)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("1000.0000")) // Welcome credit for demo simulation
                .build();
        Wallet savedWallet = walletRepository.save(wallet);

        // Auto-provision Wallet Limits
        WalletLimit limit = WalletLimit.builder()
                .walletId(savedWallet.getId())
                .dailyLimit(new BigDecimal("100000.0000"))
                .perTransactionLimit(new BigDecimal("50000.0000"))
                .dailyUsed(BigDecimal.ZERO)
                .lastResetDate(LocalDate.now())
                .build();
        walletLimitRepository.save(limit);

        // Audit Log
        recordAudit("USER_REGISTERED", String.valueOf(savedUser.getId()), "USER", "REGISTER", "Customer registered: " + savedUser.getEmail());

        // Event stream
        try {
            kafkaPublisher.send("smartpay.user.events.v1", String.valueOf(savedUser.getId()), Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", "USER_REGISTERED",
                    "eventVersion", "v1",
                    "aggregateId", String.valueOf(savedUser.getId()),
                    "occurredAt", Instant.now().toString(),
                    "payload", Map.of(
                            "userId", savedUser.getId(),
                            "email", savedUser.getEmail(),
                            "firstName", profile.getFirstName(),
                            "lastName", profile.getLastName(),
                            "walletNumber", savedWallet.getWalletNumber()
                    )
            ));
        } catch (Exception e) {
            log.warn("Failed to publish USER_REGISTERED event: {}", e.getMessage());
        }

        return createAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String email = request.email().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        // Check account lock status
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            recordAudit("LOGIN_BLOCKED", String.valueOf(user.getId()), "USER", "LOGIN_ATTEMPT", "Login blocked: Account is temporarily locked");
            throw new BusinessException("Account is temporarily locked due to multiple failed login attempts. Please try again after " + user.getLockedUntil());
        }

        // Validate password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
                user.setStatus(UserStatus.LOCKED);
                log.warn("User {} locked out due to {} failed login attempts", email, attempts);
            }
            userRepository.save(user);

            recordAudit("FAILED_LOGIN", String.valueOf(user.getId()), "USER", "LOGIN", "Failed password attempt (count: " + attempts + ")");
            throw new BusinessException("Invalid email or password");
        }

        // Check status
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.FROZEN) {
            throw new BusinessException("User account is " + user.getStatus());
        }

        // Reset failed login attempts on successful login
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            if (user.getStatus() == UserStatus.LOCKED) {
                user.setStatus(UserStatus.ACTIVE);
            }
            userRepository.save(user);
        }

        recordAudit("LOGIN_SUCCESS", String.valueOf(user.getId()), "USER", "LOGIN", "User logged in successfully");

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        // Refresh token reuse detection
        if (token.isRevoked()) {
            // Revocation of all tokens for this user due to detected reuse!
            refreshTokenRepository.revokeAllByUserId(token.getUserId());
            recordAudit("TOKEN_REUSE_DETECTED", String.valueOf(token.getUserId()), "SECURITY", "TOKEN_REVOKE_ALL", "Revoked refresh token reuse detected. All sessions terminated.");
            throw new BusinessException("Security violation: Revoked refresh token reuse detected. All active sessions have been invalidated.");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new BusinessException("Refresh token is expired");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Token rotation: revoke used token, issue fresh pair
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return createAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            recordAudit("LOGOUT", String.valueOf(token.getUserId()), "USER", "LOGOUT", "User logged out");
        });
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        if (request.currentPassword().equals(request.newPassword())) {
            throw new BusinessException("New password cannot be identical to current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Invalidate all existing refresh tokens
        refreshTokenRepository.revokeAllByUserId(userId);

        recordAudit("PASSWORD_CHANGED", String.valueOf(userId), "USER", "CHANGE_PASSWORD", "Password successfully changed. Active sessions invalidated.");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapAdminUser() {
        String adminEmail = "admin@smartpay.io";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin@SmartPay2026!"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .failedLoginAttempts(0)
                    .build();
            User saved = userRepository.save(admin);

            UserProfile profile = UserProfile.builder()
                    .userId(saved.getId())
                    .firstName("System")
                    .lastName("Administrator")
                    .kycStatus("VERIFIED")
                    .build();
            userProfileRepository.save(profile);

            log.info("Bootstrapped default system administrator account: {}", adminEmail);
        }
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenStr = tokenProvider.generateRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenStr)
                .expiryDate(Instant.now().plusMillis(604800000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getRole().name(), user.getStatus().name());
        return AuthResponse.of(accessToken, refreshTokenStr, tokenProvider.getExpirationMs(), userDto);
    }

    private void recordAudit(String eventType, String actorId, String resourceType, String action, String details) {
        try {
            auditService.recordEvent(new CreateAuditEventRequest(
                    eventType,
                    actorId,
                    "USER",
                    resourceType,
                    actorId,
                    action,
                    details,
                    "127.0.0.1",
                    UUID.randomUUID().toString()
            ));
        } catch (Exception e) {
            log.warn("Failed to record audit event: {}", e.getMessage());
        }
    }
}
