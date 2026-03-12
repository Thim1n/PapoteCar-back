package com.PapoteCar.PapoteCar.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trajets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trajet implements Serializable {

    public enum Statut { actif, termine, annule }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Utilisateur conducteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voiture_id", nullable = false)
    private Voiture voiture;

    @Column(name = "depart_rue", length = 255)
    private String departRue;

    @Column(name = "depart_ville", nullable = false, length = 100)
    private String departVille;

    @Column(name = "depart_code_postal", length = 10)
    private String departCodePostal;

    @Column(name = "depart_latitude", precision = 9, scale = 6)
    private BigDecimal departLatitude;

    @Column(name = "depart_longitude", precision = 9, scale = 6)
    private BigDecimal departLongitude;

    @Column(name = "arrivee_rue", length = 255)
    private String arriveeRue;

    @Column(name = "arrivee_ville", nullable = false, length = 100)
    private String arriveeVille;

    @Column(name = "arrivee_code_postal", length = 10)
    private String arriveeCodePostal;

    @Column(name = "arrivee_latitude", precision = 9, scale = 6)
    private BigDecimal arriveeLatitude;

    @Column(name = "arrivee_longitude", precision = 9, scale = 6)
    private BigDecimal arriveeLongitude;

    @Column(name = "horaire_depart", nullable = false)
    private LocalDateTime horaireDepart;

    @Column(name = "horaire_arrivee", nullable = false)
    private LocalDateTime horaireArrivee;

    @Column(name = "temps_trajet_min")
    private Integer tempsTrajetMin;

    @Column(name = "places_disponibles", nullable = false)
    private Integer placesDisponibles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.actif;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}