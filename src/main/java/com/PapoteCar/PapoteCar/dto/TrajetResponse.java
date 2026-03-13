package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.Trajet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Résumé d'un trajet (sans coordonnées GPS ni infos conducteur/voiture)")
@Data
@AllArgsConstructor
public class TrajetResponse {

    @Schema(description = "Identifiant unique", example = "1")
    private Integer id;

    @Schema(description = "Ville de départ", example = "Paris")
    private String departVille;

    @Schema(description = "Ville d'arrivée", example = "Lyon")
    private String arriveeVille;

    @Schema(description = "Date et heure de départ", example = "2026-04-01T08:00:00")
    private LocalDateTime horaireDepart;

    @Schema(description = "Date et heure d'arrivée", example = "2026-04-01T12:30:00")
    private LocalDateTime horaireArrivee;

    @Schema(description = "Nombre de places encore disponibles", example = "2")
    private Integer placesDisponibles;

    @Schema(description = "Prix par passager en euros", example = "15.00")
    private BigDecimal prix;

    @Schema(description = "Statut du trajet", example = "actif", allowableValues = {"actif", "termine", "annule"})
    private Trajet.Statut statut;

    @Schema(description = "Date de création du trajet", example = "2026-03-10T14:00:00")
    private LocalDateTime createdAt;
}
