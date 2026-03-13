package com.PapoteCar.PapoteCar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Données d'inscription")
@Data
public class RegisterRequest {

    @Schema(description = "Nom de famille", example = "Martin")
    private String nom;

    @Schema(description = "Prénom", example = "Alice")
    private String prenom;

    @Schema(description = "Nom d'utilisateur unique", example = "alice")
    private String username;

    @Schema(description = "Adresse email unique", example = "alice@papotecar.fr")
    private String email;

    @Schema(description = "Mot de passe (min 6 caractères recommandés)", example = "Test123")
    private String motDePasse;

    @Schema(description = "Numéro de téléphone (optionnel)", example = "0601020304")
    private String tel;
}
