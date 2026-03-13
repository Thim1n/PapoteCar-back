package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.TailleCoffre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "Détail d'une voiture")
@Data
@AllArgsConstructor
public class VoitureResponse {

    @Schema(description = "Identifiant unique", example = "1")
    private Integer id;

    @Schema(description = "Modèle du véhicule", example = "Renault Zoé")
    private String modele;

    @Schema(description = "Capacité maximale en passagers (hors conducteur)", example = "3")
    private Integer nbPassagers;

    @Schema(description = "Couleur", example = "Blanche", nullable = true)
    private String couleur;

    @Schema(description = "Taille du coffre", example = "Moyen", nullable = true, allowableValues = {"Petit", "Moyen", "Grand"})
    private TailleCoffre tailleCoffre;
}
