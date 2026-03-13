package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.SellerRequest;
import com.backend.gamesales.Services.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @PostMapping("/request")
    public ResponseEntity<?> requestSeller(@RequestBody SellerRequest request, Authentication authentication){
         sellerService.requestSeller(request,authentication);

         return ResponseEntity.ok("Seller request Successfully");
    }

}
