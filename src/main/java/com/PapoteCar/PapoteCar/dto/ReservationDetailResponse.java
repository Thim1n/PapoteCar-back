package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReservationDetailResponse {
    private Integer id;
    private Integer trajetId;
    private String trajetDepartVille;
    private String trajetArriveeVille;
    private LocalDateTime trajetHoraireDepart;
    private Integer passagerId;
    private String passagerNom;
    private String passagerPrenom;
    private Reservation.Statut statut;
    private LocalDateTime createdAt;
}
