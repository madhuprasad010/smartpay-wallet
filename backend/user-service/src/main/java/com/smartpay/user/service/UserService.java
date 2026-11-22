package com.smartpay.user.service;

import com.smartpay.user.dto.UpdateProfileRequest;
import com.smartpay.user.dto.UserProfileDto;
import com.smartpay.user.entity.UserProfile;
import com.smartpay.user.exception.ResourceNotFoundException;
import com.smartpay.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().userId(userId).build());
        return mapToDto(profile);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().userId(userId).build());

        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setAddress(request.address());
        profile.setDateOfBirth(request.dateOfBirth());

        UserProfile saved = userProfileRepository.save(profile);
        return mapToDto(saved);
    }

    private UserProfileDto mapToDto(UserProfile profile) {
        return new UserProfileDto(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getDateOfBirth()
        );
    }
}
