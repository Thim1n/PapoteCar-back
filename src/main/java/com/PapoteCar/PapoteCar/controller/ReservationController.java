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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Réservation", description = "Cycle de vie d'une réservation : en_attente → valide/refuse → annule")
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

    @Operation(
            summary = "S'inscrire sur un trajet (passager)",
            description = """
                    Crée une réservation en statut `en_attente`. Les places ne sont pas décomptées avant validation.

                    **Guards (dans l'ordre) :**
                    1. 404 si le trajet n'existe pas
                    2. 409 si le trajet n'est pas en statut `actif`
                    3. 400 si l'utilisateur connecté est le conducteur du trajet
                    4. 409 si une réservation existe déjà pour ce passager sur ce trajet
                    5. 409 si plus de places disponibles (`placesDisponibles` = 0)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réservation créée en statut en_attente"),
            @ApiResponse(responseCode = "400", description = "Le passager est le conducteur du trajet",
                    content = @Content(schema = @Schema(example = "Vous ne pouvez pas réserver votre propre trajet"))),
            @ApiResponse(responseCode = "404", description = "Trajet introuvable",
                    content = @Content(schema = @Schema(example = "Trajet introuvable"))),
            @ApiResponse(responseCode = "409", description = "Trajet inactif, déjà réservé par ce passager, ou trajet complet",
                    content = @Content(schema = @Schema(example = "Ce trajet est complet, il ne reste plus de places disponibles")))
    })
    @PostMapping("/trajets/{id}/reservations")
    public ResponseEntity<?> creerReservation(
            @Parameter(description = "ID du trajet", example = "5") @PathVariable Integer id) {
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

    @Operation(
            summary = "Liste des passagers d'un trajet (conducteur)",
            description = "Retourne toutes les réservations du trajet avec les informations de chaque passager (nom, prénom, statut). Accès réservé au conducteur du trajet."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réservations avec infos passagers"),
            @ApiResponse(responseCode = "403", description = "L'utilisateur connecté n'est pas conducteur de ce trajet",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Trajet introuvable",
                    content = @Content(schema = @Schema(example = "Trajet introuvable")))
    })
    @GetMapping("/trajets/{id}/reservations")
    public ResponseEntity<List<ReservationAvecPassagerResponse>> getReservationsTrajet(
            @Parameter(description = "ID du trajet", example = "1") @PathVariable Integer id) {
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

    @Operation(
            summary = "Détail d'une réservation",
            description = """
                    Retourne le détail complet d'une réservation (trajet + passager + statut).

                    **Double autorisation :** passager de la réservation OU conducteur du trajet.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Détail de la réservation"),
            @ApiResponse(responseCode = "403", description = "L'utilisateur n'est ni le passager ni le conducteur du trajet",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable",
                    content = @Content(schema = @Schema(example = "Réservation introuvable")))
    })
    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationDetailResponse> getReservation(
            @Parameter(description = "ID de la réservation", example = "1") @PathVariable Integer id) {
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

    @Operation(
            summary = "Annuler sa réservation (passager)",
            description = """
                    Le passager annule sa propre réservation.

                    **Effets selon le statut actuel (`@Transactional`) :**
                    - `valide` → statut passe à `annule` + `trajet.placesDisponibles + 1` + `passager.solde + trajet.prix` (remboursement)
                    - `en_attente` ou `refuse` → statut passe à `annule`, aucun impact sur les places ni le solde

                    **Impossible si déjà `annule` (409).**
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Réservation annulée (remboursement si statut était valide)"),
            @ApiResponse(responseCode = "403", description = "L'utilisateur connecté n'est pas le passager de cette réservation",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable",
                    content = @Content(schema = @Schema(example = "Réservation introuvable"))),
            @ApiResponse(responseCode = "409", description = "La réservation est déjà annulée",
                    content = @Content(schema = @Schema(example = "Cette réservation est déjà annulée")))
    })
    @Transactional
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<?> annulerReservation(
            @Parameter(description = "ID de la réservation", example = "1") @PathVariable Integer id) {
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

    @Operation(
            summary = "Valider un passager (conducteur)",
            description = """
                    Le conducteur accepte une demande de réservation en attente.

                    **Effets atomiques (`@Transactional`) :**
                    - `reservation.statut` → `valide`
                    - `trajet.placesDisponibles - 1`
                    - `passager.solde - trajet.prix`

                    **Guards (dans l'ordre) :**
                    1. 403 si l'utilisateur connecté n'est pas conducteur du trajet
                    2. 409 si la réservation n'est pas en statut `en_attente`
                    3. 409 si plus aucune place disponible sur le trajet
                    4. 409 si le solde du passager est insuffisant (solde < prix du trajet)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation validée — places et solde mis à jour"),
            @ApiResponse(responseCode = "403", description = "L'utilisateur connecté n'est pas conducteur du trajet",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable",
                    content = @Content(schema = @Schema(example = "Réservation introuvable"))),
            @ApiResponse(responseCode = "409", description = "Statut ≠ en_attente, trajet complet, ou solde passager insuffisant",
                    content = @Content(schema = @Schema(example = "Impossible de valider : le passager ne dispose pas d'un solde suffisant")))
    })
    @Transactional
    @PutMapping("/reservations/{id}/valider")
    public ResponseEntity<?> validerReservation(
            @Parameter(description = "ID de la réservation", example = "1") @PathVariable Integer id) {
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

    @Operation(
            summary = "Refuser un passager (conducteur)",
            description = """
                    Le conducteur refuse une demande de réservation en attente.

                    **Effets :** `reservation.statut` → `refuse`. Aucune modification des places ni du solde.

                    **Impossible si la réservation n'est pas en statut `en_attente` (409).**
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation refusée"),
            @ApiResponse(responseCode = "403", description = "L'utilisateur connecté n'est pas conducteur du trajet",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable",
                    content = @Content(schema = @Schema(example = "Réservation introuvable"))),
            @ApiResponse(responseCode = "409", description = "La réservation n'est pas en statut en_attente",
                    content = @Content(schema = @Schema(example = "Impossible de refuser cette réservation : elle est en statut valide")))
    })
    @PutMapping("/reservations/{id}/refuser")
    public ResponseEntity<?> refuserReservation(
            @Parameter(description = "ID de la réservation", example = "1") @PathVariable Integer id) {
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
