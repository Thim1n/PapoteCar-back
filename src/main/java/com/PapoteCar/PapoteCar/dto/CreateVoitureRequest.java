package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.TailleCoffre;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateVoitureRequest {
    private String modele;
    private Integer nbPassagers;
    private String couleur;
    private TailleCoffre tailleCoffre;
}
