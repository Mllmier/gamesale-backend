package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.SellerRequest;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.StatusSeller;
import com.backend.gamesales.Model.TypeSeller;
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



    public void requestSeller(SellerRequest request, Authentication authentication){

        Users users=usersRepository.findByEmail(authentication.getName())
                .orElseThrow(()->new RuntimeException("User not found"));


           if(sellerRepository.existsByUser(users)){
               throw new RuntimeException("User already has a seller request");
           }
        Seller seller=new Seller();
        seller.setDescription(request.getDescription());
        seller.setTypeSeller(request.getTypeSeller());


        if(seller.getStatus() != StatusSeller.APPROVED){
            throw new RuntimeException("Seller not approved yet");
        }
        if(seller.getTypeSeller() == TypeSeller.COMPANY){
            if(seller.getCompanyName() == null || seller.getCompanyId() == null){
                throw new RuntimeException("Company information required");
            }
        }


        seller.setStatus(StatusSeller.PENDING);
        seller.setRating(0.0);
        seller.setCreatedAt(LocalDateTime.now());
        seller.setUser(users);

        sellerRepository.save(seller);
    }


}
