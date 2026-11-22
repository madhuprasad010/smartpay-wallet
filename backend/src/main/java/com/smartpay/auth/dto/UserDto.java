package com.smartpay.auth.dto;

public record UserDto(
        Long id,
        String email,
        String role,
        String status
) {}
