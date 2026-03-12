package com.PapoteCar.PapoteCar.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UpdateTrajetRequest {
    private String departRue;
    private String departVille;
    private String departCodePostal;
    private BigDecimal departLatitude;
    private BigDecimal departLongitude;

    private String arriveeRue;
    private String arriveeVille;
    private String arriveeCodePostal;
    private BigDecimal arriveeLatitude;
    private BigDecimal arriveeLongitude;

    private Integer voitureId;
    private LocalDateTime horaireDepart;
    private LocalDateTime horaireArrivee;
    private Integer tempsTrajetMin;
    private Integer placesDisponibles;
    private BigDecimal prix;
}
