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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
    public ResponseEntity<?> creerReservation(@PathVariable Integer id) {
        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));

        if (trajet.getStatut() != Trajet.Statut.actif) {
            log.warn("Conflit réservation trajet id={} : trajet inactif (statut={})", id, trajet.getStatut());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de réserver ce trajet : il n'est plus actif");
        }

        Utilisateur connecte = utilisateurConnecte();

        if (trajet.getConducteur().getId().equals(connecte.getId())) {
            log.warn("Réservation refusée : utilisateur id={} tente de réserver son propre trajet id={}", connecte.getId(), id);
            return ResponseEntity.badRequest().body("Vous ne pouvez pas réserver votre propre trajet");
        }

        if (reservationRepository.findByTrajetIdAndPassagerId(id, connecte.getId()).isPresent()) {
            log.warn("Conflit réservation trajet id={} : passager id={} a déjà une réservation", id, connecte.getId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Vous avez déjà une réservation sur ce trajet");
        }

        if (trajet.getPlacesDisponibles() <= 0) {
            log.warn("Conflit réservation trajet id={} : plus de places disponibles", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce trajet est complet, il ne reste plus de places disponibles");
        }

        Reservation reservation = new Reservation();
        reservation.setTrajet(trajet);
        reservation.setPassager(connecte);
        reservation.setStatut(Reservation.Statut.en_attente);
        reservation.setCreatedAt(LocalDateTime.now());

        Reservation sauvegardee = reservationRepository.save(reservation);
        log.info("Réservation créée id={} par passager id={} sur trajet id={}", sauvegardee.getId(), connecte.getId(), id);

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
            log.warn("Accès interdit : utilisateur id={} tente de lister les réservations du trajet id={}", connecte.getId(), id);
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
            log.warn("Accès interdit : utilisateur id={} tente d'accéder à la réservation id={}", connecte.getId(), id);
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
    public ResponseEntity<?> annulerReservation(@PathVariable Integer id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!reservation.getPassager().getId().equals(connecte.getId())) {
            log.warn("Accès interdit : utilisateur id={} tente d'annuler la réservation id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (reservation.getStatut() == Reservation.Statut.annule) {
            log.warn("Conflit annulation réservation id={} : déjà annulée", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cette réservation est déjà annulée");
        }

        if (reservation.getStatut() == Reservation.Statut.valide) {
            Trajet trajet = reservation.getTrajet();
            trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() + 1);
            trajetRepository.save(trajet);

            Utilisateur passager = reservation.getPassager();
            if (trajet.getPrix() != null) {
                passager.setSolde(passager.getSolde().add(trajet.getPrix()));
                utilisateurRepository.save(passager);
            }
        }

        reservation.setStatut(Reservation.Statut.annule);
        reservationRepository.save(reservation);
        log.info("Réservation annulée id={} par passager id={}", id, connecte.getId());

        return ResponseEntity.noContent().build();
    }

    // PUT /reservations/{id}/valider — Valider un passager (conducteur)
    @Transactional
    @PutMapping("/reservations/{id}/valider")
    public ResponseEntity<?> validerReservation(@PathVariable Integer id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!reservation.getTrajet().getConducteur().getId().equals(connecte.getId())) {
            log.warn("Accès interdit : utilisateur id={} tente de valider la réservation id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (reservation.getStatut() != Reservation.Statut.en_attente) {
            log.warn("Conflit validation réservation id={} : statut={}", id, reservation.getStatut());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de valider cette réservation : elle est en statut " + reservation.getStatut());
        }

        Trajet trajet = reservation.getTrajet();
        if (trajet.getPlacesDisponibles() <= 0) {
            log.warn("Conflit validation réservation id={} : trajet id={} complet", id, trajet.getId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de valider : le trajet est complet");
        }

        Utilisateur passager = reservation.getPassager();
        if (passager.getSolde().compareTo(trajet.getPrix()) < 0) {
            log.warn("Conflit validation réservation id={} : solde insuffisant pour passager id={}", id, passager.getId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de valider : le passager ne dispose pas d'un solde suffisant");
        }

        reservation.setStatut(Reservation.Statut.valide);
        reservationRepository.save(reservation);

        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - 1);
        trajetRepository.save(trajet);

        passager.setSolde(passager.getSolde().subtract(trajet.getPrix()));
        utilisateurRepository.save(passager);

        log.info("Réservation validée id={} par conducteur id={}, passager id={}", id, connecte.getId(), passager.getId());
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
    public ResponseEntity<?> refuserReservation(@PathVariable Integer id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!reservation.getTrajet().getConducteur().getId().equals(connecte.getId())) {
            log.warn("Accès interdit : utilisateur id={} tente de refuser la réservation id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (reservation.getStatut() != Reservation.Statut.en_attente) {
            log.warn("Conflit refus réservation id={} : statut={}", id, reservation.getStatut());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de refuser cette réservation : elle est en statut " + reservation.getStatut());
        }

        reservation.setStatut(Reservation.Statut.refuse);
        reservationRepository.save(reservation);
        log.info("Réservation refusée id={} par conducteur id={}", id, connecte.getId());

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
        log.error("Erreur inattendue dans ReservationController : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
