package com.backend.gamesales.Dto;


import com.backend.gamesales.Model.Enums.TypeSeller;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SellerRequest {

    @NotBlank(message = "The description is required")
    @Size(max = 500, message = "Maxium 500 caracteres")
    private String description;
    @NotNull(message = "The type seller is required ")
    private TypeSeller typeSeller;
    private String companyName;
    private String companyId;

}
