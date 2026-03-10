package com.PapoteCar.PapoteCar.repository;

import com.PapoteCar.PapoteCar.model.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrajetRepository extends JpaRepository<Trajet, Integer> {
    boolean existsByConducteurIdAndStatut(Integer conducteurId, Trajet.Statut statut);
    List<Trajet> findByConducteurId(Integer conducteurId);
}