package com.PapoteCar.PapoteCar.dto;

import com.PapoteCar.PapoteCar.model.Trajet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TrajetDetailResponse {
    private Integer id;
    private String conducteurNom;
    private String conducteurPrenom;
    private String voitureModele;
    private Integer voitureNbPassagers;
    private String departVille;
    private String departRue;
    private String arriveeVille;
    private String arriveeRue;
    private LocalDateTime horaireDepart;
    private LocalDateTime horaireArrivee;
    private Integer tempsTrajetMin;
    private Integer placesDisponibles;
    private Trajet.Statut statut;
}