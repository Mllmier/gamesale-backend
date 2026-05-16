package com.backend.gamesales.Dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class GameRequest {
    @NotBlank(message = "The title is required")
    private String title;

    @NotNull(message = "The price is required")
    @Min(value = 0, message = "The price cannot be negative.")
    private BigDecimal price;

    @NotBlank(message = "The description is required")
    private String description;

    @NotBlank(message = "The develop is required")
    private String developer;

    @NotBlank(message = "The published is required")
    private String publisher;

    @NotBlank(message = "The release date is mandatory")
    private String releaseDate;

    @NotBlank(message = "The category is required")
    private String category;

    private MultipartFile image;
    private Set<Long> tagIds;
}
