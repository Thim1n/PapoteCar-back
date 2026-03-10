package com.PapoteCar.PapoteCar.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateUtilisateurRequest {
    private String nom;
    private String prenom;
    private String tel;
}