package com.backend.gamesales.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class GameRequest {
    private String title;
    private Double price;
    private String description;
    private String developer;
    private String publisher;
    private String releaseDate;
    private String category;
    private MultipartFile image;
    private Set<Long> tagIds;
}
