package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.TailleCoffre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Données pour créer une voiture")
@Data
@NoArgsConstructor
public class CreateVoitureRequest {

    @Schema(description = "Modèle du véhicule (obligatoire)", example = "Renault Zoé")
    private String modele;

    @Schema(description = "Capacité maximale en passagers, hors conducteur (obligatoire, ≥ 1). Limite le champ placesDisponibles lors de la création d'un trajet.", example = "3")
    private Integer nbPassagers;

    @Schema(description = "Couleur du véhicule (optionnel)", example = "Blanche")
    private String couleur;

    @Schema(description = "Taille du coffre (optionnel)", example = "Moyen", allowableValues = {"Petit", "Moyen", "Grand"})
    private TailleCoffre tailleCoffre;
}
