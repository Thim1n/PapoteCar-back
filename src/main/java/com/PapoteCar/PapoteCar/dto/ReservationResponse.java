package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Résumé d'une réservation (retourné après création, validation ou refus)")
@Data
@AllArgsConstructor
public class ReservationResponse {

    @Schema(description = "Identifiant de la réservation", example = "1")
    private Integer id;

    @Schema(description = "Identifiant du trajet", example = "1")
    private Integer trajetId;

    @Schema(description = "Ville de départ du trajet", example = "Paris")
    private String trajetDepartVille;

    @Schema(description = "Ville d'arrivée du trajet", example = "Lyon")
    private String trajetArriveeVille;

    @Schema(description = "Horaire de départ du trajet", example = "2026-04-01T08:00:00")
    private LocalDateTime trajetHoraireDepart;

    @Schema(description = "Statut de la réservation", example = "en_attente", allowableValues = {"en_attente", "valide", "refuse", "annule"})
    private Reservation.Statut statut;

    @Schema(description = "Date de création de la réservation", example = "2026-03-13T10:00:00")
    private LocalDateTime createdAt;
}
