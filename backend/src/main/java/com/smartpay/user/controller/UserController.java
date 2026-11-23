package com.smartpay.user.controller;

import com.smartpay.common.dto.ApiResponse;
import com.smartpay.user.dto.*;
import com.smartpay.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(@PathVariable Long userId) {
        UserProfileDto profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto profile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @PostMapping("/{userId}/kyc")
    public ResponseEntity<ApiResponse<UserProfileDto>> submitKyc(
            @PathVariable Long userId,
            @Valid @RequestBody KycSubmitRequest request) {
        UserProfileDto profile = userService.submitKyc(userId, request);
        return ResponseEntity.ok(ApiResponse.success("KYC submitted successfully and is pending review", profile));
    }

    @PostMapping("/{userId}/kyc/verify")
    public ResponseEntity<ApiResponse<UserProfileDto>> verifyKyc(@PathVariable Long userId) {
        UserProfileDto profile = userService.verifyKyc(userId);
        return ResponseEntity.ok(ApiResponse.success("KYC approved and verified successfully", profile));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserSummaryDto>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserSummaryDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserSummaryDto>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserSummaryDto user = userService.updateUserStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User account status updated to " + request.status(), user));
    }
}
