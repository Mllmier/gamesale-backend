package com.backend.gamesales.Services;

import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Repository.GameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public Game uploadGame(Game game){

        if(game.getPrice() <= 0){
            throw new RuntimeException("Price must be greater than 0");
        }

        if(game.getTitle() == null || game.getTitle().isEmpty()){
            throw new RuntimeException("Title is required");
        }
        if(game.getDescription() == null || game.getDescription().isEmpty()){
            throw new RuntimeException("Description is required");
        }

        return gameRepository.save(game);
    }

}
