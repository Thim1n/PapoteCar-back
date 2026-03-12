package com.PapoteCar.PapoteCar.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CreateTrajetRequest {
    // Adresse départ
    private String departRue;
    private String departVille;
    private String departCodePostal;
    // Coordonnées départ
    private BigDecimal departLatitude;
    private BigDecimal departLongitude;

    // Adresse arrivée
    private String arriveeRue;
    private String arriveeVille;
    private String arriveeCodePostal;
    // Coordonnées arrivée
    private BigDecimal arriveeLatitude;
    private BigDecimal arriveeLongitude;

    private Integer voitureId;
    private LocalDateTime horaireDepart;
    private LocalDateTime horaireArrivee;
    private Integer tempsTrajetMin;
    private Integer placesDisponibles;
    private BigDecimal prix;
}