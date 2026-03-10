package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.Trajet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TrajetResponse {
    private Integer id;
    private String departVille;
    private String arriveeVille;
    private LocalDateTime horaireDepart;
    private LocalDateTime horaireArrivee;
    private Integer placesDisponibles;
    private Trajet.Statut statut;
    private LocalDateTime createdAt;
}