package com.PapoteCar.PapoteCar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Données pour créer un trajet")
@Data
@NoArgsConstructor
public class CreateTrajetRequest {

    @Schema(description = "Rue de départ (optionnel, pour affichage)", example = "10 Rue de Rivoli")
    private String departRue;

    @Schema(description = "Ville de départ (recommandée pour la recherche par ville)", example = "Paris")
    private String departVille;

    @Schema(description = "Code postal de départ (optionnel)", example = "75001")
    private String departCodePostal;

    @Schema(description = "Latitude du point de départ — fournie par l'API adresse.data.gouv.fr ou Google Maps, nécessaire pour la recherche GPS", example = "48.856614")
    private BigDecimal departLatitude;

    @Schema(description = "Longitude du point de départ", example = "2.352222")
    private BigDecimal departLongitude;

    @Schema(description = "Rue d'arrivée (optionnel, pour affichage)", example = "5 Place Bellecour")
    private String arriveeRue;

    @Schema(description = "Ville d'arrivée (recommandée pour la recherche par ville)", example = "Lyon")
    private String arriveeVille;

    @Schema(description = "Code postal d'arrivée (optionnel)", example = "69002")
    private String arriveeCodePostal;

    @Schema(description = "Latitude du point d'arrivée", example = "45.757814")
    private BigDecimal arriveeLatitude;

    @Schema(description = "Longitude du point d'arrivée", example = "4.832011")
    private BigDecimal arriveeLongitude;

    @Schema(description = "ID de la voiture à utiliser (doit appartenir à l'utilisateur connecté)", example = "1")
    private Integer voitureId;

    @Schema(description = "Date et heure de départ (obligatoire, ISO 8601)", example = "2026-04-01T08:00:00")
    private LocalDateTime horaireDepart;

    @Schema(description = "Date et heure d'arrivée (optionnel si tempsTrajetMin fourni)", example = "2026-04-01T12:30:00")
    private LocalDateTime horaireArrivee;

    @Schema(description = "Durée estimée en minutes (utilisée pour calculer horaireArrivee si absent)", example = "270")
    private Integer tempsTrajetMin;

    @Schema(description = "Nombre de places disponibles pour les passagers (≤ capacité voiture)", example = "3")
    private Integer placesDisponibles;

    @Schema(description = "Prix par passager en euros (optionnel, défaut 0.00)", example = "15.00")
    private BigDecimal prix;
}
