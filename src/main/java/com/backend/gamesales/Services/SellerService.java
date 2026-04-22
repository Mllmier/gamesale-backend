package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.SellerRequest;
import com.backend.gamesales.Dto.SellerResponse;
import com.backend.gamesales.Dto.SellerStatusResponse;
import com.backend.gamesales.Model.Enums.Role;
import com.backend.gamesales.Model.Enums.TypeSeller;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Enums.StatusSeller;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.SellerRepository;
import com.backend.gamesales.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private UsersRepository usersRepository;


    public SellerResponse requestSeller(SellerRequest request, Users user) {
        if (user.getProfile() == null) {
            throw new RuntimeException("Complete your profile before requesting to be a seller");
        }
        if (sellerRepository.existsByUser(user)) {
            throw new RuntimeException("You already have a seller request registered");
        }
        if (request.getTypeSeller() == TypeSeller.LEGAL_ENTITY &&
                (request.getCompanyName() == null || request.getCompanyName().isBlank())) {
            throw new RuntimeException("Company name is required for legal entity sellers");
        }

        Seller seller = new Seller();
        applySellerFields(seller, request);
        seller.setStatus(StatusSeller.PENDING);
        seller.setRating(0.0);
        seller.setUser(user);

        sellerRepository.save(seller);
        return toSellerResponse(user, seller);
    }


    public SellerStatusResponse getSellerStatus(Users user) {
        if (user.getSeller() == null) {
            return SellerStatusResponse.builder()
                    .status(null)
                    .message("You have not submitted a request to become a seller yet")
                    .build();
        }

        Seller seller = user.getSeller();

        String message = switch (seller.getStatus()) {
            case PENDING   -> "Your request is under review. We will notify you soon.";
            case APPROVED  -> "Your store is active. You can start selling.";
            case REJECTED  -> "Your request was rejected. You can apply again.";
        };

        return SellerStatusResponse.builder()
                .status(seller.getStatus())
                .createdAt(seller.getCreatedAt())
                .verifiedAt(seller.getVerifiedAt())
                .message(message)
                .build();
    }


    public SellerResponse getSellerProfile(Users user) {
        return toSellerResponse(user, getSellerOrThrow(user));
    }

    public SellerResponse updateSellerProfile(Users user, SellerRequest request) {
        Seller seller = getSellerOrThrow(user);

        if (request.getTypeSeller() == TypeSeller.LEGAL_ENTITY &&
                (request.getCompanyName() == null || request.getCompanyName().isBlank())) {
            throw new RuntimeException("Company name is required for legal entity sellers");
        }

        applySellerFields(seller, request);
        sellerRepository.save(seller);
        return toSellerResponse(user, seller);
    }


    public SellerResponse getPublicProfile(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        return toSellerResponse(seller.getUser(), seller);
    }


    public void approveSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller request not found"));

        if (seller.getStatus() != StatusSeller.PENDING) {
            throw new RuntimeException("This request has already been processed");
        }

        seller.setStatus(StatusSeller.APPROVED);
        seller.setVerifiedAt(LocalDateTime.now());
        sellerRepository.save(seller);

        Users user = seller.getUser();
        user.setRole(Role.SELLER);
        usersRepository.save(user);
    }

    public void rejectSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller request not found"));

        if (seller.getStatus() != StatusSeller.PENDING) {
            throw new RuntimeException("This request has already been processed");
        }

        seller.setStatus(StatusSeller.REJECTED);
        sellerRepository.save(seller);
    }

    public void suspendSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (seller.getStatus() != StatusSeller.APPROVED) {
            throw new RuntimeException("Only active sellers can be suspended");
        }

        sellerRepository.save(seller);
    }


    private void applySellerFields(Seller seller, SellerRequest request) {
        seller.setDescription(request.getDescription());
        seller.setTypeSeller(request.getTypeSeller());
        seller.setCompanyName(request.getCompanyName());
        seller.setCompanyId(request.getCompanyId());
    }

    public Seller getSellerOrThrow(Users user) {
        if (user.getSeller() == null) {
            throw new RuntimeException("This user does not have a registered store");
        }
        return user.getSeller();
    }

    private SellerResponse toSellerResponse(Users user, Seller seller) {
        return SellerResponse.builder()
                .description(seller.getDescription())
                .typeSeller(seller.getTypeSeller())
                .companyName(seller.getCompanyName())
                .companyId(seller.getCompanyId())
                .status(seller.getStatus())
                .rating(seller.getRating())
                .createdAt(seller.getCreatedAt())
                .build();
    }
}
