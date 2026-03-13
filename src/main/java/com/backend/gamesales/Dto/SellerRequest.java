package com.backend.gamesales.Dto;


import com.backend.gamesales.Model.TypeSeller;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SellerRequest {

    private String description;
    private TypeSeller typeSeller;

}
