package com.PapoteCar.PapoteCar.repository;

import com.PapoteCar.PapoteCar.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByTrajetId(Integer trajetId);
}
