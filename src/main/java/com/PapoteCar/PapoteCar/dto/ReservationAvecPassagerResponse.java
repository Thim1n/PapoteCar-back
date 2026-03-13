package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Réservation avec informations du passager — retourné dans la liste des réservations d'un trajet (vue conducteur)")
@Data
@AllArgsConstructor
public class ReservationAvecPassagerResponse {

    @Schema(description = "Identifiant de la réservation", example = "1")
    private Integer id;

    @Schema(description = "Identifiant du passager", example = "2")
    private Integer passagerId;

    @Schema(description = "Nom du passager", example = "Dupont")
    private String passagerNom;

    @Schema(description = "Prénom du passager", example = "Bob")
    private String passagerPrenom;

    @Schema(description = "Statut de la réservation", example = "en_attente", allowableValues = {"en_attente", "valide", "refuse", "annule"})
    private Reservation.Statut statut;

    @Schema(description = "Date de création de la réservation", example = "2026-03-13T10:00:00")
    private LocalDateTime createdAt;
}
