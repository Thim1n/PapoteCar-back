package com.PapoteCar.PapoteCar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Profil d'un utilisateur. Les champs email, tel et solde sont null si ce n'est pas le propre profil de l'utilisateur connecté.")
@Data
@AllArgsConstructor
public class UtilisateurResponse {

    @Schema(description = "Identifiant unique", example = "1")
    private Integer id;

    @Schema(description = "Nom de famille", example = "Martin")
    private String nom;

    @Schema(description = "Prénom", example = "Alice")
    private String prenom;

    @Schema(description = "Téléphone — null si profil d'un autre utilisateur", example = "0601020304", nullable = true)
    private String tel;

    @Schema(description = "Date de création du compte", example = "2026-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Email — null si profil d'un autre utilisateur", example = "alice@papotecar.fr", nullable = true)
    private String email;

    @Schema(description = "Permis de conduire validé (requis pour créer des trajets)", example = "true")
    private boolean permisDeConduire;

    @Schema(description = "Solde en euros — null si profil d'un autre utilisateur", example = "200.00", nullable = true)
    private BigDecimal solde;
}
