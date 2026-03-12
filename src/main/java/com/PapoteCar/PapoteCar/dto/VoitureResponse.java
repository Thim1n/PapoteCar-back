package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.TailleCoffre;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoitureResponse {
    private Integer id;
    private String modele;
    private Integer nbPassagers;
    private String couleur;
    private TailleCoffre tailleCoffre;
}
