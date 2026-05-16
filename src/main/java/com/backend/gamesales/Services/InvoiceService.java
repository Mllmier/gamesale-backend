package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Response.InvoiceResponse;
import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Infrastructure.EmailSender;
import com.backend.gamesales.Infrastructure.MailBody;
import com.backend.gamesales.Infrastructure.PdfGenerator;
import com.backend.gamesales.Model.Invoice;
import com.backend.gamesales.Model.Payment;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PdfGenerator pdfGenerator;
    private final EmailSender emailSender;

    @Transactional
    public Invoice createAndSendInvoice(Payment payment, String gameTitles, Users buyer) {
        if (invoiceRepository.existsByPaymentId(payment.getId())) {
            log.warn("Invoice already exists | paymentId: {}", payment.getId());
            return invoiceRepository.findByPaymentId(payment.getId()).orElseThrow();
        }

        Invoice invoice = invoiceRepository.save(buildInvoice(payment, gameTitles, buyer));
        sendInvoiceByEmail(invoice);

        log.info("Invoice created and sent | invoiceNumber: {} | to: {}",
                invoice.getInvoiceNumber(), invoice.getBuyerEmail());

        return invoice;
    }

    public byte[] downloadInvoice(Long invoiceId, Long buyerId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found: " + invoiceId));

        if (!invoice.getBuyerId().equals(buyerId)) {
            throw new NotFoundException("Access denied: not your invoice");
        }

        return pdfGenerator.generateInvoicePdf(invoice);
    }

    public List<InvoiceResponse> getMyInvoices(Long buyerId) {
        return invoiceRepository.findByBuyerIdOrderByIssuedAtDesc(buyerId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }


    private Invoice buildInvoice(Payment payment, String gameTitles, Users buyer) {
        return Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .buyerId(buyer.getId())
                .payment(payment)
                .gameTitle(gameTitles)
                .amount(payment.getTotal())
                .currency("COP")
                .buyerEmail(buyer.getEmail())
                .buyerFullName(resolveFullName(buyer))
                .buyerCountry(buyer.getProfile() != null ? buyer.getProfile().getCountry() : null)
                .build();
    }

    private void sendInvoiceByEmail(Invoice invoice) {
        byte[] pdf = pdfGenerator.generateInvoicePdf(invoice);

        emailSender.sendEmailWithAttachment(
                new MailBody(
                        invoice.getBuyerEmail(),
                        "Your GameSales Invoice - " + invoice.getInvoiceNumber(),
                        buildEmailBody(invoice)
                ),
                pdf,
                "invoice-" + invoice.getInvoiceNumber() + ".pdf"
        );
    }

    private String buildEmailBody(Invoice invoice) {
        String name = invoice.getBuyerFullName() != null ? invoice.getBuyerFullName() : "";
        return "Hi " + name + ",\n\n" +
                "Thank you for your purchase of " + invoice.getGameTitle() + ".\n\n" +
                "Please find your invoice attached.\n" +
                "You can also download it anytime from your account.\n\n" +
                "Thanks for using GameSales!\n" +
                "The GameSales Team";
    }

    private String generateInvoiceNumber() {
        return String.format("INV-%05d-%s",
                invoiceRepository.count() + 1,
                UUID.randomUUID().toString().substring(0, 4).toUpperCase());
    }

    private String resolveFullName(Users buyer) {
        if (buyer.getProfile() == null) return null;

        String firstName = buyer.getProfile().getFirstName();
        String lastName  = buyer.getProfile().getLastName();

        if (firstName == null && lastName == null) return null;
        if (firstName == null) return lastName;
        if (lastName  == null) return firstName;

        return firstName + " " + lastName;
    }

    private InvoiceResponse toResponseDTO(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .gameTitle(invoice.getGameTitle())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .buyerEmail(invoice.getBuyerEmail())
                .buyerFullName(invoice.getBuyerFullName())
                .issuedAt(invoice.getIssuedAt())
                .build();
    }
}
