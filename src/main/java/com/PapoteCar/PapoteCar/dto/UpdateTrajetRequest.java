package com.PapoteCar.PapoteCar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Modification partielle d'un trajet — tous les champs sont optionnels (null = inchangé). Si l'adresse est modifiée, envoyer les champs GPS correspondants en même temps.")
@Data
@NoArgsConstructor
public class UpdateTrajetRequest {

    @Schema(description = "Nouvelle rue de départ", example = "15 Rue de Rivoli")
    private String departRue;

    @Schema(description = "Nouvelle ville de départ", example = "Paris")
    private String departVille;

    @Schema(description = "Nouveau code postal de départ", example = "75001")
    private String departCodePostal;

    @Schema(description = "Nouvelle latitude de départ", example = "48.860611")
    private BigDecimal departLatitude;

    @Schema(description = "Nouvelle longitude de départ", example = "2.351499")
    private BigDecimal departLongitude;

    @Schema(description = "Nouvelle rue d'arrivée", example = "2 Place Bellecour")
    private String arriveeRue;

    @Schema(description = "Nouvelle ville d'arrivée", example = "Lyon")
    private String arriveeVille;

    @Schema(description = "Nouveau code postal d'arrivée", example = "69002")
    private String arriveeCodePostal;

    @Schema(description = "Nouvelle latitude d'arrivée", example = "45.757814")
    private BigDecimal arriveeLatitude;

    @Schema(description = "Nouvelle longitude d'arrivée", example = "4.832011")
    private BigDecimal arriveeLongitude;

    @Schema(description = "ID de la nouvelle voiture (doit appartenir à l'utilisateur connecté)", example = "2")
    private Integer voitureId;

    @Schema(description = "Nouvel horaire de départ", example = "2026-04-01T09:00:00")
    private LocalDateTime horaireDepart;

    @Schema(description = "Nouvel horaire d'arrivée", example = "2026-04-01T13:30:00")
    private LocalDateTime horaireArrivee;

    @Schema(description = "Nouvelle durée estimée en minutes", example = "270")
    private Integer tempsTrajetMin;

    @Schema(description = "Nouveau nombre de places disponibles", example = "2")
    private Integer placesDisponibles;

    @Schema(description = "Nouveau prix par passager en euros", example = "12.00")
    private BigDecimal prix;
}
