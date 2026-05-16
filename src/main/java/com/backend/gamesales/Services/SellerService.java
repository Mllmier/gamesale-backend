package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Request.SellerRequest;
import com.backend.gamesales.Dto.Response.RejectReasonResponse;
import com.backend.gamesales.Dto.Response.SellerResponse;
import com.backend.gamesales.Dto.Response.SellerStatusResponse;
import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Infrastructure.EmailSender;
import com.backend.gamesales.Infrastructure.StripeConnect;
import com.backend.gamesales.Model.Enums.Role;
import com.backend.gamesales.Model.Enums.TypeSeller;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Enums.StatusSeller;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.SellerRepository;
import com.backend.gamesales.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UsersRepository usersRepository;
    private final StripeConnect stripeConnect;
    private final EmailSender emailSender;

    public SellerResponse requestSeller(SellerRequest request, Users user) {
        if (user.getProfile() == null) {
            throw new RuntimeException(" Complete your profile before applying to be a seller");
        }
        if (sellerRepository.existsByUser(user)) {
            throw new RuntimeException("You already have a registered seller application.");
        }
        if (request.getTypeSeller() == TypeSeller.LEGAL_ENTITY &&
                (request.getCompanyName() == null || request.getCompanyName().isBlank())) {
            throw new RuntimeException("The company name is mandatory for legal entities.");
        }

        Seller seller = new Seller();
        applySellerFields(seller, request);
        seller.setStatus(StatusSeller.PENDING);
        seller.setRating(0.0);
        seller.setUser(user);

        seller = sellerRepository.save(seller);
        return toSellerResponse(user, seller);
    }

    public SellerStatusResponse getSellerStatus(Users user) {
        Seller seller = sellerRepository.findByUserId(user.getId()).orElse(null);

        if (seller == null) {
            return SellerStatusResponse.builder()
                    .status(null)
                    .message("You haven't yet submitted an application to become a seller.")
                    .build();
        }

        String message = switch (seller.getStatus()) {
            case PENDING   -> "Your application is under review. We will notify you soon.";
            case APPROVED  -> "Your store is live. You can start selling.";
            case REJECTED  ->  buildRejectedMessage(seller.getRejectionReason());
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
            throw new RuntimeException("The company name is mandatory for legal entities.");
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
            throw new RuntimeException("This request has already been processed.");
        }

        seller.setStatus(StatusSeller.APPROVED);
        seller.setVerifiedAt(LocalDateTime.now());
        sellerRepository.save(seller);

        Users user = seller.getUser();
        user.setRole(Role.SELLER);
        usersRepository.save(user);
    }

    public RejectReasonResponse rejectSeller(Long id, String reason) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller request not found"));

        if (seller.getStatus() != StatusSeller.PENDING) {
            throw new RuntimeException("This request has already been processed.");
        }

        seller.setStatus(StatusSeller.REJECTED);
        seller.setRejectionReason(reason);
        sellerRepository.save(seller);

        return RejectReasonResponse.builder()
                .message("Seller rejected successfully")
                .reason(reason)
                .build();
    }

    private void applySellerFields(Seller seller, SellerRequest request) {
        seller.setStoreName(request.getStoreName());
        seller.setDescription(request.getDescription());
        seller.setTypeSeller(request.getTypeSeller());
        seller.setCompanyName(request.getCompanyName());
        seller.setCompanyId(request.getCompanyId());
    }

    public Seller getSellerOrThrow(Users user) {
        return sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("This user does not have a registered store."));
    }
    private String buildRejectedMessage(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Your request was rejected. You can try again.";
        }
        return "Your request was rejected: " + reason;
    }

    private SellerResponse toSellerResponse(Users user, Seller seller) {
        return SellerResponse.builder()
                .id(seller.getId())
                .storeName(seller.getStoreName())
                .description(seller.getDescription())
                .typeSeller(seller.getTypeSeller())
                .companyName(seller.getCompanyName())
                .companyId(seller.getCompanyId())
                .status(seller.getStatus())
                .rating(seller.getRating())
                .createdAt(seller.getCreatedAt())
                .build();
    }
    public void deactivateMyAccount(String email) {
        Seller seller = sellerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Seller not found"));
        seller.setActive(false);
        sellerRepository.save(seller);
    }
}
