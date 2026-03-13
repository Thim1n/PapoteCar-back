package com.PapoteCar.PapoteCar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Réponse d'authentification contenant le token JWT")
@Data
@AllArgsConstructor
public class AuthResponse {

    @Schema(description = "Token JWT à placer dans le header Authorization : Bearer <token>",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Date/heure d'expiration du token (minuit du jour courant)",
            example = "2026-03-13T23:59:59")
    private LocalDateTime expireA;
}
