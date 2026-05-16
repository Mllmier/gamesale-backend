package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Request.ProfileRequest;
import com.backend.gamesales.Dto.Response.ProfileResponse;
import com.backend.gamesales.Infrastructure.Storage.Storage;
import com.backend.gamesales.Model.Profile;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.SellerRepository;
import com.backend.gamesales.Repository.UsersRepository;
import com.backend.gamesales.Utils.PasswordValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileService {
    private final UsersRepository usersRepository;
    private final SellerRepository sellerRepository;
    private final Storage storage;
    private static final List<String> DEFAULT_AVATARS = List.of(
            "https://TU_PROYECTO.supabase.co/storage/v1/object/public/TU_BUCKET/avatars/defaults/avatar1.png",
            "https://TU_PROYECTO.supabase.co/storage/v1/object/public/TU_BUCKET/avatars/defaults/avatar2.png",
            "https://TU_PROYECTO.supabase.co/storage/v1/object/public/TU_BUCKET/avatars/defaults/avatar3.png"
    );


    public ProfileResponse getProfile(Users user) {
        Profile profile = user.getProfile();
        if (profile == null) {
            return ProfileResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .build();
        }
        return toProfileResponse(user, profile);
    }

    public ProfileResponse updateProfile(Users user, ProfileRequest request) {
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            usersRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new RuntimeException("The email is already in use.");
                }
            });
            user.setEmail(request.getEmail());
        }

        applyProfileFields(profile, request);
        usersRepository.save(user);
        return toProfileResponse(user, profile);
    }



    private void applyProfileFields(Profile profile, ProfileRequest request) {
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setBio(request.getBio());
        profile.setCountry(request.getCountry());
        profile.setPhoneNumber(request.getPhoneNumber());
    }




    private ProfileResponse toProfileResponse(Users user, Profile profile) {
        return ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .bio(profile.getBio())
                .avatar(profile.getAvatarUrl())
                .country(profile.getCountry())
                .phoneNumber(profile.getPhoneNumber())
                .build();
    }



    private Users getRefreshedUser(Users user) {
        return usersRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<String> getDefaultAvatars() {
        return DEFAULT_AVATARS;
    }


    public ProfileResponse selectAvatar(Users user, String avatarUrl) {
        Users freshUser = getRefreshedUser(user);
        Profile profile = freshUser.getProfile();

        if (profile == null) {
            throw new RuntimeException("Complete your profile before changing your avatar");
        }


        if (!DEFAULT_AVATARS.contains(avatarUrl)) {
            throw new RuntimeException("Invalid  Avatar ");
        }


        if (profile.getAvatarUrl() != null
                && !profile.getAvatarUrl().isBlank()
                && profile.getAvatarUrl().contains("/avatars/")
                && !profile.getAvatarUrl().contains("/defaults/")) {
            storage.deleteImage(profile.getAvatarUrl());
        }

        profile.setAvatarUrl(avatarUrl);
        usersRepository.save(freshUser);
        return toProfileResponse(freshUser, profile);
    }

    public ProfileResponse updateAvatar(Users user, MultipartFile image) {
        Users freshUser = getRefreshedUser(user);
        Profile profile = freshUser.getProfile();

        if (profile == null) {
            throw new RuntimeException("Complete your profile before changing your avatar");
        }

        String newAvatarUrl = storage.replaceAvatar(
                profile.getAvatarUrl(), image, freshUser.getId()
        );
        profile.setAvatarUrl(newAvatarUrl);
        usersRepository.save(freshUser);
        return toProfileResponse(freshUser, profile);
    }

    public void changePasswordAuthenticated(Users user, String currentPassword,
                                            String newPassword, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("The password is incorrect");
        }
        if (!PasswordValidator.isValid(newPassword)) {
            throw new RuntimeException(
                    "The password must be at least 8 characters long and include letters and numbers.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
    }

}
