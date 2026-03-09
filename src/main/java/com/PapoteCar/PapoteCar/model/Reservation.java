package com.PapoteCar.PapoteCar.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation implements Serializable {

    public enum Statut { en_attente, valide, refuse, annule }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passager_id", nullable = false)
    private Utilisateur passager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.en_attente;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}