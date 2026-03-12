package com.PapoteCar.PapoteCar.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateVoitureRequest {
    private String modele;
    private Integer nbPassagers;
    private String couleur;
    private Integer tailleCoffre;
}
