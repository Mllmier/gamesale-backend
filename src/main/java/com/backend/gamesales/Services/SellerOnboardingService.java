package com.backend.gamesales.Services;

import com.backend.gamesales.Infrastructure.StripeConnect;
import com.backend.gamesales.Model.Enums.StatusSeller;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerOnboardingService {

    private final SellerRepository sellerRepository;
    private final StripeConnect stripeConnect;


    public String iniciarOnboarding(Users user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (seller.getStatus() != StatusSeller.APPROVED) {
            throw new RuntimeException("Seller is not approved yet");
        }
        if (seller.getStripeAccountId() != null) {
            log.info("Regenerating onboarding link for seller={}", seller.getId());
            return stripeConnect.createAccountLink(seller.getStripeAccountId());
        }

        String accountId = stripeConnect.createConnectedAccount(
                seller.getUser().getEmail(),
                seller.getStoreName()
        );

        seller.setStripeAccountId(accountId);
        sellerRepository.save(seller);

        log.info("Stripe account created | sellerId={} | accountId={}",
                seller.getId(), accountId);

        return stripeConnect.createAccountLink(accountId);
    }
}