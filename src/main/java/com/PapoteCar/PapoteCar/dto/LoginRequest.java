package com.PapoteCar.PapoteCar.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Données de connexion")
@Data
public class LoginRequest {

    @JsonAlias({"email", "username"})
    @Schema(
            description = "Email ou username de l'utilisateur. Le champ accepte indifféremment `login`, `email` ou `username` en JSON.",
            example = "alice@papotecar.fr"
    )
    private String login;

    @Schema(description = "Mot de passe", example = "Test123")
    private String motDePasse;
}
