package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.Trajet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Détail complet d'un trajet avec infos conducteur, voiture, adresses GPS et horaires")
@Data
@AllArgsConstructor
public class TrajetDetailResponse {

    @Schema(description = "Identifiant unique", example = "1")
    private Integer id;

    @Schema(description = "Nom du conducteur", example = "Martin")
    private String conducteurNom;

    @Schema(description = "Prénom du conducteur", example = "Alice")
    private String conducteurPrenom;

    @Schema(description = "Modèle de la voiture", example = "Renault Zoé")
    private String voitureModele;

    @Schema(description = "Capacité totale de la voiture (hors conducteur)", example = "3")
    private Integer voitureNbPassagers;

    @Schema(description = "Rue de départ", example = "10 Rue de Rivoli", nullable = true)
    private String departRue;

    @Schema(description = "Ville de départ", example = "Paris")
    private String departVille;

    @Schema(description = "Code postal de départ", example = "75001", nullable = true)
    private String departCodePostal;

    @Schema(description = "Latitude du point de départ", example = "48.856614", nullable = true)
    private BigDecimal departLatitude;

    @Schema(description = "Longitude du point de départ", example = "2.352222", nullable = true)
    private BigDecimal departLongitude;

    @Schema(description = "Rue d'arrivée", example = "5 Place Bellecour", nullable = true)
    private String arriveeRue;

    @Schema(description = "Ville d'arrivée", example = "Lyon")
    private String arriveeVille;

    @Schema(description = "Code postal d'arrivée", example = "69002", nullable = true)
    private String arriveeCodePostal;

    @Schema(description = "Latitude du point d'arrivée", example = "45.757814", nullable = true)
    private BigDecimal arriveeLatitude;

    @Schema(description = "Longitude du point d'arrivée", example = "4.832011", nullable = true)
    private BigDecimal arriveeLongitude;

    @Schema(description = "Date et heure de départ", example = "2026-04-01T08:00:00")
    private LocalDateTime horaireDepart;

    @Schema(description = "Date et heure d'arrivée", example = "2026-04-01T12:30:00")
    private LocalDateTime horaireArrivee;

    @Schema(description = "Durée estimée en minutes", example = "270", nullable = true)
    private Integer tempsTrajetMin;

    @Schema(description = "Nombre de places encore disponibles", example = "2")
    private Integer placesDisponibles;

    @Schema(description = "Prix par passager en euros", example = "15.00")
    private BigDecimal prix;

    @Schema(description = "Statut du trajet", example = "actif", allowableValues = {"actif", "termine", "annule"})
    private Trajet.Statut statut;
}
