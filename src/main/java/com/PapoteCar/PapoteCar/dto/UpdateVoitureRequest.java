package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.TailleCoffre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Modification partielle d'une voiture — tous les champs sont optionnels (null = inchangé)")
@Data
@NoArgsConstructor
public class UpdateVoitureRequest {

    @Schema(description = "Nouveau modèle (null = inchangé, chaîne vide = erreur 400)", example = "Peugeot e-208")
    private String modele;

    @Schema(description = "Nouvelle capacité passagers (null = inchangé, < 1 = erreur 400)", example = "4")
    private Integer nbPassagers;

    @Schema(description = "Nouvelle couleur (null = inchangé)", example = "Grise")
    private String couleur;

    @Schema(description = "Nouvelle taille de coffre (null = inchangé)", example = "Grand", allowableValues = {"Petit", "Moyen", "Grand"})
    private TailleCoffre tailleCoffre;
}
