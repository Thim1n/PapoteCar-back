package com.PapoteCar.PapoteCar.repository;

import com.PapoteCar.PapoteCar.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByTrajetId(Integer trajetId);

    List<Reservation> findByPassagerId(Integer passagerId);

    Optional<Reservation> findByTrajetIdAndPassagerId(Integer trajetId, Integer passagerId);

    List<Reservation> findByTrajetIdAndStatut(Integer trajetId, Reservation.Statut statut);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.trajet t JOIN FETCH t.conducteur JOIN FETCH r.passager WHERE r.id = :id")
    Optional<Reservation> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.passager WHERE r.trajet.id = :trajetId")
    List<Reservation> findByTrajetIdWithPassager(@Param("trajetId") Integer trajetId);
}
