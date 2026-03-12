package com.PapoteCar.PapoteCar.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UpdateTrajetRequest {
    private String departVille;
    private String departRue;
    private String arriveeVille;
    private String arriveeRue;
    private Integer voitureId;
    private LocalDateTime horaireDepart;
    private LocalDateTime horaireArrivee;
    private Integer tempsTrajetMin;
    private Integer placesDisponibles;
}
