package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.ProfileRequest;
import com.backend.gamesales.Dto.ProfileResponse;
import com.backend.gamesales.Dto.SellerRequest;
import com.backend.gamesales.Dto.SellerResponse;
import com.backend.gamesales.Model.Enums.Role;
import com.backend.gamesales.Model.Enums.StatusSeller;
import com.backend.gamesales.Model.Enums.TypeSeller;
import com.backend.gamesales.Model.Profile;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.SellerRepository;
import com.backend.gamesales.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileService {
    private final UsersRepository usersRepository;
    private final SellerRepository sellerRepository;
    private final StorageService storageService;
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
            throw new RuntimeException("Completa tu perfil antes de cambiar el avatar");
        }


        if (!DEFAULT_AVATARS.contains(avatarUrl)) {
            throw new RuntimeException("Avatar no válido");
        }


        if (profile.getAvatarUrl() != null
                && !profile.getAvatarUrl().isBlank()
                && profile.getAvatarUrl().contains("/avatars/")
                && !profile.getAvatarUrl().contains("/defaults/")) {
            storageService.deleteImage(profile.getAvatarUrl());
        }

        profile.setAvatarUrl(avatarUrl);
        usersRepository.save(freshUser);
        return toProfileResponse(freshUser, profile);
    }

    public ProfileResponse updateAvatar(Users user, MultipartFile image) {
        Users freshUser = getRefreshedUser(user);
        Profile profile = freshUser.getProfile();

        if (profile == null) {
            throw new RuntimeException("Completa tu perfil antes de cambiar el avatar");
        }

        String newAvatarUrl = storageService.replaceAvatar(
                profile.getAvatarUrl(), image, freshUser.getId()
        );
        profile.setAvatarUrl(newAvatarUrl);
        usersRepository.save(freshUser);
        return toProfileResponse(freshUser, profile);
    }
}
