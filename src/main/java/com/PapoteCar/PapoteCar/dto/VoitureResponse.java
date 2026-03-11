package com.PapoteCar.PapoteCar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoitureResponse {
    private Integer id;
    private String modele;
    private Integer nbPassagers;
}
