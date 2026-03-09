package com.PapoteCar.PapoteCar.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "voitures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voiture implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 100)
    private String modele;

    @Column(name = "nb_passagers", nullable = false)
    private Integer nbPassagers;
}