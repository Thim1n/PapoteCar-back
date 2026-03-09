package com.PapoteCar.PapoteCar.repository;

import com.PapoteCar.PapoteCar.model.Voiture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoitureRepository extends JpaRepository<Voiture, Integer> {
}