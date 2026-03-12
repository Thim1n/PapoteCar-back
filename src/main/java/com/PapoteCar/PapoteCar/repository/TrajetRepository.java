package com.PapoteCar.PapoteCar.repository;

import com.PapoteCar.PapoteCar.model.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface TrajetRepository extends JpaRepository<Trajet, Integer> {
    boolean existsByConducteurIdAndStatut(Integer conducteurId, Trajet.Statut statut);
    List<Trajet> findByConducteurId(Integer conducteurId);
    boolean existsByVoitureIdAndStatut(Integer voitureId, Trajet.Statut statut);

    @Query("SELECT t FROM Trajet t JOIN FETCH t.conducteur JOIN FETCH t.voiture " +
           "WHERE t.statut = com.PapoteCar.PapoteCar.model.Trajet.Statut.actif " +
           "AND (:departVille IS NULL OR LOWER(t.departVille) LIKE LOWER(CONCAT('%', :departVille, '%'))) " +
           "AND (:arriveeVille IS NULL OR LOWER(t.arriveeVille) LIKE LOWER(CONCAT('%', :arriveeVille, '%'))) " +
           "AND (:dateDebut IS NULL OR t.horaireDepart >= :dateDebut) " +
           "AND (:dateFin IS NULL OR t.horaireDepart < :dateFin) " +
           "AND (:placesMin IS NULL OR t.placesDisponibles >= :placesMin) " +
           "ORDER BY t.horaireDepart ASC")
    List<Trajet> searchTrajets(
            @Param("departVille") String departVille,
            @Param("arriveeVille") String arriveeVille,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin,
            @Param("placesMin") Integer placesMin
    );

    @Query("SELECT t FROM Trajet t JOIN FETCH t.conducteur JOIN FETCH t.voiture WHERE t.id = :id")
    Optional<Trajet> findByIdWithDetails(@Param("id") Integer id);
}
