package com.backend.gamesales.Infrastructure;

import com.backend.gamesales.Config.StripeConfig;
import com.backend.gamesales.Exceptions.PaymentException;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import com.stripe.model.Transfer;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.TransferCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripeConnect {

    private final StripeConfig stripeConfig;
    private static final String DEFAULT_COUNTRY = "CO";
    private static final String TRANSFER_CURRENCY   = "usd";


    public String createConnectedAccount(String email, String name) {
        try {
            AccountCreateParams params = buildAccountParams(email, name);
            Account account = Account.create(params);

            log.info("Nueva cuenta creada dinámicamente | ID: {}", account.getId());
            return account.getId();

        } catch (StripeException e) {
            log.error("Error al crear cuenta dinámica: {}", e.getMessage());
            throw new PaymentException("No se pudo crear la cuenta en Stripe", e);
        }
    }

    public String createAccountLink(String stripeAccountId) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(stripeAccountId)
                    .setRefreshUrl(stripeConfig.getOnboardingRefreshUrl())
                    .setReturnUrl(stripeConfig.getOnboardingReturnUrl())
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            return AccountLink.create(params).getUrl();
        } catch (StripeException e) {
            log.error("Error generando link para {}: {}", stripeAccountId, e.getMessage());
            throw new PaymentException("Error en el registro de Stripe", e);
        }
    }

    public String transferToSeller(String destinationAccountId, BigDecimal amountCop,
                                   String reference, String chargeId) {
        try {
            BigDecimal amountUsd = convertCopToUsd(amountCop, chargeId);
            TransferCreateParams params = buildTransferParams(destinationAccountId, amountUsd, reference, chargeId);
            Transfer transfer = Transfer.create(params);
            log.info("Transfer created | transferId={} | destination={} | amountCop={} | amountUsd={}",
                    transfer.getId(), destinationAccountId, amountCop, amountUsd);
            return transfer.getId();
        } catch (StripeException e) {
            log.error("Failed to transfer to {} | error={}", destinationAccountId, e.getMessage());
            throw new PaymentException("Transfer failed", e);
        }
    }
    private BigDecimal convertCopToUsd(BigDecimal amountCop, String chargeId) throws StripeException {
        Charge charge = Charge.retrieve(chargeId);
        BalanceTransaction balanceTx = BalanceTransaction.retrieve(charge.getBalanceTransaction());

        BigDecimal exchangeRate = balanceTx.getExchangeRate();

        if (exchangeRate == null) {
            throw new PaymentException("No exchange rate found for charge: " + chargeId);
        }

        BigDecimal amountUsd = amountCop
                .multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Currency conversion | chargeId={} | amountCop={} | exchangeRate={} | amountUsd={}",
                chargeId, amountCop, exchangeRate, amountUsd);

        return amountUsd;
    }

    private AccountCreateParams buildAccountParams(String email, String name) {
        SellerName sellerName = SellerName.from(name);
        return AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setCountry(DEFAULT_COUNTRY)
                .setEmail(email)
                .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                                .setTransfers(
                                        AccountCreateParams.Capabilities.Transfers.builder()
                                                .setRequested(true)
                                                .build()
                                )
                                .build()
                )
                .setTosAcceptance(
                        AccountCreateParams.TosAcceptance.builder()
                                .setServiceAgreement("recipient")
                                .build()
                )
                .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                .setIndividual(AccountCreateParams.Individual.builder()
                        .setFirstName(sellerName.firstName())
                        .setLastName(sellerName.lastName())
                        .build())
                .build();
    }

    private TransferCreateParams buildTransferParams(String destination, BigDecimal amountUsd,
                                                     String reference, String chargeId) {
        return TransferCreateParams.builder()
                .setAmount(toCents(amountUsd))
                .setCurrency(TRANSFER_CURRENCY)
                .setDestination(destination)
                .setSourceTransaction(chargeId)
                .build();
    }
    private long toCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private record SellerName(String firstName, String lastName) {
        static SellerName from(String fullName) {
            String[] parts = fullName.split(" ", 2);
            return new SellerName(parts[0], parts.length > 1 ? parts[1] : "Seller");
        }
    }
}