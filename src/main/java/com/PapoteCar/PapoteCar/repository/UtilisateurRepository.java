package com.PapoteCar.PapoteCar.repository;

import com.PapoteCar.PapoteCar.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
}