package com.backend.gamesales.Services;

import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Infrastructure.Storage.Storage;
import com.backend.gamesales.Model.Enums.StatusSeller;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.GameRepository;
import com.backend.gamesales.Repository.SellerRepository;
import com.backend.gamesales.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SellerRepository sellerRepository;
    private final UsersRepository usersRepository;
    private final GameRepository gameRepository;

    public Page<Seller> getPendingSellers(Pageable pageable) {
        return sellerRepository.findByStatus(StatusSeller.PENDING, pageable);
    }

    public Page<Users> getAllUsers(Pageable pageable) {
        return usersRepository.findAll(pageable);
    }

    public void deleteGameByAdmin(Long gameId, Storage storage) {
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found : " + gameId));
        if (game.getImageUrl() != null) storage.deleteImage(game.getImageUrl());
        gameRepository.delete(game);
    }
    public void deactivateSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));
        seller.setActive(false);
        sellerRepository.save(seller);
    }
}

