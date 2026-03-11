package com.PapoteCar.PapoteCar.repository;

import com.PapoteCar.PapoteCar.model.Voiture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoitureRepository extends JpaRepository<Voiture, Integer> {
    List<Voiture> findByUtilisateurId(Integer utilisateurId);
    boolean existsByIdAndUtilisateurId(Integer id, Integer utilisateurId);
}