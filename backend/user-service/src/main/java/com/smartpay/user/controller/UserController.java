package com.smartpay.user.controller;

import com.smartpay.user.dto.ApiResponse;
import com.smartpay.user.dto.UpdateProfileRequest;
import com.smartpay.user.dto.UserProfileDto;
import com.smartpay.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(@PathVariable Long userId) {
        UserProfileDto profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }
}
