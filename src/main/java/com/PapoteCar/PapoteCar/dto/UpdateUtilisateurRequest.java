package com.PapoteCar.PapoteCar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Modification partielle du profil — tous les champs sont optionnels")
@Data
@NoArgsConstructor
public class UpdateUtilisateurRequest {

    @Schema(description = "Nouveau nom de famille (null = inchangé)", example = "Dupont")
    private String nom;

    @Schema(description = "Nouveau prénom (null = inchangé)", example = "Jean-Pierre")
    private String prenom;

    @Schema(description = "Nouveau numéro de téléphone (null = inchangé)", example = "0699887766")
    private String tel;
}
