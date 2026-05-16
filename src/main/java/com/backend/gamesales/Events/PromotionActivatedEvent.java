package com.backend.gamesales.Events;

import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Promotion;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PromotionActivatedEvent extends ApplicationEvent {

    private final Promotion promotion;
    private final Game game;

    public PromotionActivatedEvent(Object source, Promotion promotion, Game game) {
        super(source);
        this.promotion = promotion;
        this.game = game;
    }
}