package com.PapoteCar.PapoteCar.repository;

import com.PapoteCar.PapoteCar.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
}