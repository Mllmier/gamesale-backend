package com.backend.gamesales.Controller;

import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.SellerRepository;
import com.backend.gamesales.Services.GameService;
import com.backend.gamesales.Utils.ErrorResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.backend.gamesales.Utils.ErrorResponseBuilder.buildErrorResponse;

@RestController
@RequestMapping ("api/game")
public class GameController {

    @Autowired
    private GameService gameservice;

    @Autowired
    private SellerRepository sellerRepository;

    @PostMapping("/upload")
    public  ResponseEntity<?> upload(@RequestBody Game game, Authentication authentication){
        try {
            Users user = (Users) authentication.getPrincipal();

            Game savedGame = gameservice.uploadGame(game);

            Seller seller = sellerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Seller not found"));

            game.setSeller(seller);
            game.setSeller(seller);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

}
