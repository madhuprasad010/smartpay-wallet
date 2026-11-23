package com.smartpay.user.service;

import com.smartpay.auth.entity.User;
import com.smartpay.auth.repository.UserRepository;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.user.dto.KycSubmitRequest;
import com.smartpay.user.dto.UpdateProfileRequest;
import com.smartpay.user.dto.UpdateUserStatusRequest;
import com.smartpay.user.dto.UserProfileDto;
import com.smartpay.user.dto.UserSummaryDto;
import com.smartpay.user.entity.UserProfile;
import com.smartpay.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().userId(userId).kycStatus("UNVERIFIED").build());
        return mapToDto(profile);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().userId(userId).kycStatus("UNVERIFIED").build());

        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setAddress(request.address());
        profile.setDateOfBirth(request.dateOfBirth());

        UserProfile saved = userProfileRepository.save(profile);
        return mapToDto(saved);
    }

    @Transactional
    public UserProfileDto submitKyc(Long userId, KycSubmitRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().userId(userId).build());

        profile.setKycDocumentType(request.documentType());
        profile.setKycDocumentNumber(request.documentNumber());
        profile.setKycStatus("PENDING");

        UserProfile saved = userProfileRepository.save(profile);
        return mapToDto(saved);
    }

    @Transactional
    public UserProfileDto verifyKyc(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));

        profile.setKycStatus("VERIFIED");
        profile.setKycVerifiedAt(Instant.now());

        UserProfile saved = userProfileRepository.save(profile);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            UserProfile profile = userProfileRepository.findByUserId(user.getId())
                    .orElse(UserProfile.builder().kycStatus("UNVERIFIED").build());
            return new UserSummaryDto(
                    user.getId(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus(),
                    profile.getFirstName(),
                    profile.getLastName(),
                    profile.getKycStatus(),
                    user.getCreatedAt()
            );
        });
    }

    @Transactional
    public UserSummaryDto updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus(request.status());
        User saved = userRepository.save(user);

        UserProfile profile = userProfileRepository.findByUserId(saved.getId())
                .orElse(UserProfile.builder().kycStatus("UNVERIFIED").build());

        return new UserSummaryDto(
                saved.getId(),
                saved.getEmail(),
                saved.getRole(),
                saved.getStatus(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getKycStatus(),
                saved.getCreatedAt()
        );
    }

    private UserProfileDto mapToDto(UserProfile profile) {
        return new UserProfileDto(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getDateOfBirth(),
                profile.getKycStatus() != null ? profile.getKycStatus() : "UNVERIFIED",
                profile.getKycDocumentType(),
                profile.getKycDocumentNumber(),
                profile.getKycVerifiedAt()
        );
    }
}
