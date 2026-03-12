package com.PapoteCar.PapoteCar.controller;

import com.PapoteCar.PapoteCar.dto.ReservationAvecPassagerResponse;
import com.PapoteCar.PapoteCar.dto.ReservationDetailResponse;
import com.PapoteCar.PapoteCar.dto.ReservationResponse;
import com.PapoteCar.PapoteCar.model.Reservation;
import com.PapoteCar.PapoteCar.model.Trajet;
import com.PapoteCar.PapoteCar.model.Utilisateur;
import com.PapoteCar.PapoteCar.repository.ReservationRepository;
import com.PapoteCar.PapoteCar.repository.TrajetRepository;
import com.PapoteCar.PapoteCar.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final UtilisateurRepository utilisateurRepository;

    private Utilisateur utilisateurConnecte() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }

    // POST /trajets/{id}/reservations — S'inscrire sur un trajet
    @PostMapping("/trajets/{id}/reservations")
    public ResponseEntity<ReservationResponse> creerReservation(@PathVariable Integer id) {
        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));

        if (trajet.getStatut() != Trajet.Statut.actif) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Utilisateur connecte = utilisateurConnecte();

        if (trajet.getConducteur().getId().equals(connecte.getId())) {
            return ResponseEntity.badRequest().build();
        }

        if (reservationRepository.findByTrajetIdAndPassagerId(id, connecte.getId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (trajet.getPlacesDisponibles() <= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Reservation reservation = new Reservation();
        reservation.setTrajet(trajet);
        reservation.setPassager(connecte);
        reservation.setStatut(Reservation.Statut.en_attente);
        reservation.setCreatedAt(LocalDateTime.now());

        Reservation sauvegardee = reservationRepository.save(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ReservationResponse(
                sauvegardee.getId(),
                trajet.getId(),
                trajet.getDepartVille(),
                trajet.getArriveeVille(),
                trajet.getHoraireDepart(),
                sauvegardee.getStatut(),
                sauvegardee.getCreatedAt()
        ));
    }

    // GET /trajets/{id}/reservations — Liste des passagers (conducteur uniquement)
    @GetMapping("/trajets/{id}/reservations")
    public ResponseEntity<List<ReservationAvecPassagerResponse>> getReservationsTrajet(@PathVariable Integer id) {
        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!trajet.getConducteur().getId().equals(connecte.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ReservationAvecPassagerResponse> reservations = reservationRepository
                .findByTrajetIdWithPassager(id)
                .stream()
                .map(reservation -> new ReservationAvecPassagerResponse(
                        reservation.getId(),
                        reservation.getPassager().getId(),
                        reservation.getPassager().getNom(),
                        reservation.getPassager().getPrenom(),
                        reservation.getStatut(),
                        reservation.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(reservations);
    }

    // GET /reservations/{id} — Détail d'une réservation (passager OU conducteur du trajet)
    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationDetailResponse> getReservation(@PathVariable Integer id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        boolean estPassager = reservation.getPassager().getId().equals(connecte.getId());
        boolean estConducteur = reservation.getTrajet().getConducteur().getId().equals(connecte.getId());

        if (!estPassager && !estConducteur) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(new ReservationDetailResponse(
                reservation.getId(),
                reservation.getTrajet().getId(),
                reservation.getTrajet().getDepartVille(),
                reservation.getTrajet().getArriveeVille(),
                reservation.getTrajet().getHoraireDepart(),
                reservation.getPassager().getId(),
                reservation.getPassager().getNom(),
                reservation.getPassager().getPrenom(),
                reservation.getStatut(),
                reservation.getCreatedAt()
        ));
    }

    // DELETE /reservations/{id} — Se désinscrire (passager)
    @Transactional
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> annulerReservation(@PathVariable Integer id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!reservation.getPassager().getId().equals(connecte.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (reservation.getStatut() == Reservation.Statut.annule) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (reservation.getStatut() == Reservation.Statut.valide) {
            Trajet trajet = reservation.getTrajet();
            trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() + 1);
            trajetRepository.save(trajet);
        }

        reservation.setStatut(Reservation.Statut.annule);
        reservationRepository.save(reservation);

        return ResponseEntity.noContent().build();
    }

    // PUT /reservations/{id}/valider — Valider un passager (conducteur)
    @Transactional
    @PutMapping("/reservations/{id}/valider")
    public ResponseEntity<ReservationResponse> validerReservation(@PathVariable Integer id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!reservation.getTrajet().getConducteur().getId().equals(connecte.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (reservation.getStatut() != Reservation.Statut.en_attente) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Trajet trajet = reservation.getTrajet();
        if (trajet.getPlacesDisponibles() <= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        reservation.setStatut(Reservation.Statut.valide);
        reservationRepository.save(reservation);

        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - 1);
        trajetRepository.save(trajet);

        return ResponseEntity.ok(new ReservationResponse(
                reservation.getId(),
                trajet.getId(),
                trajet.getDepartVille(),
                trajet.getArriveeVille(),
                trajet.getHoraireDepart(),
                reservation.getStatut(),
                reservation.getCreatedAt()
        ));
    }

    // PUT /reservations/{id}/refuser — Refuser un passager (conducteur)
    @PutMapping("/reservations/{id}/refuser")
    public ResponseEntity<ReservationResponse> refuserReservation(@PathVariable Integer id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!reservation.getTrajet().getConducteur().getId().equals(connecte.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (reservation.getStatut() != Reservation.Statut.en_attente) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        reservation.setStatut(Reservation.Statut.refuse);
        reservationRepository.save(reservation);

        Trajet trajet = reservation.getTrajet();
        return ResponseEntity.ok(new ReservationResponse(
                reservation.getId(),
                trajet.getId(),
                trajet.getDepartVille(),
                trajet.getArriveeVille(),
                trajet.getHoraireDepart(),
                reservation.getStatut(),
                reservation.getCreatedAt()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
