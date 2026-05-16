package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Response.InvoiceResponse;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invoice ")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices(
            @AuthenticationPrincipal Users buyer) {

        return ResponseEntity.ok(invoiceService.getMyInvoices(buyer.getId()));
    }

    @GetMapping("/{invoiceId}/download")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long invoiceId,
            @AuthenticationPrincipal Users buyer) {

        byte[] pdf = invoiceService.downloadInvoice(invoiceId, buyer.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-" + invoiceId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
