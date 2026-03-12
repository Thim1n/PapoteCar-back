package com.PapoteCar.PapoteCar.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateVoitureRequest {
    private String modele;          // null = pas de modification
    private Integer nbPassagers;    // null = pas de modification
    private String couleur;         // null = pas de modification
    private Integer tailleCoffre;   // null = pas de modification
}
