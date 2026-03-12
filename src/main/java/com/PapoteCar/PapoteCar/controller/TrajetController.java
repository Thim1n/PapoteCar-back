package com.PapoteCar.PapoteCar.controller;

import com.PapoteCar.PapoteCar.dto.CreateTrajetRequest;
import com.PapoteCar.PapoteCar.dto.TrajetDetailResponse;
import com.PapoteCar.PapoteCar.dto.TrajetResponse;
import com.PapoteCar.PapoteCar.dto.UpdateTrajetRequest;
import com.PapoteCar.PapoteCar.model.Reservation;
import com.PapoteCar.PapoteCar.model.Trajet;
import com.PapoteCar.PapoteCar.model.Utilisateur;
import com.PapoteCar.PapoteCar.model.Voiture;
import com.PapoteCar.PapoteCar.repository.ReservationRepository;
import com.PapoteCar.PapoteCar.repository.TrajetRepository;
import com.PapoteCar.PapoteCar.repository.UtilisateurRepository;
import com.PapoteCar.PapoteCar.repository.VoitureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetRepository trajetRepository;
    private final VoitureRepository voitureRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ReservationRepository reservationRepository;

    private Utilisateur utilisateurConnecte() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }

    // GET /trajets/mes-trajets — déclaré AVANT /trajets/{id} pour éviter les conflits
    @GetMapping("/trajets/mes-trajets")
    public ResponseEntity<List<TrajetResponse>> getMesTrajets() {
        Utilisateur connecte = utilisateurConnecte();
        List<TrajetResponse> trajets = trajetRepository.findByConducteurId(connecte.getId())
                .stream()
                .map(trajet -> new TrajetResponse(
                        trajet.getId(),
                        trajet.getDepartVille(),
                        trajet.getArriveeVille(),
                        trajet.getHoraireDepart(),
                        trajet.getHoraireArrivee(),
                        trajet.getPlacesDisponibles(),
                        trajet.getStatut(),
                        trajet.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(trajets);
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // GET /trajets — recherche avec filtres optionnels (ville ou GPS)
    @GetMapping("/trajets")
    public ResponseEntity<List<TrajetDetailResponse>> searchTrajets(
            @RequestParam(required = false) String departVille,
            @RequestParam(required = false) String arriveeVille,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer placesMin,
            @RequestParam(required = false) Double departLat,
            @RequestParam(required = false) Double departLon,
            @RequestParam(required = false) Double arriveeLat,
            @RequestParam(required = false) Double arriveeLon,
            @RequestParam(required = false, defaultValue = "20.0") Double rayonKm) {

        LocalDateTime dateDebut = null;
        LocalDateTime dateFin = null;
        if (date != null) {
            dateDebut = date.atStartOfDay();
            dateFin = date.plusDays(1).atStartOfDay();
        }

        boolean modeGps = (departLat != null && departLon != null) || (arriveeLat != null && arriveeLon != null);

        List<Trajet> trajets;
        if (modeGps) {
            trajets = trajetRepository.searchTrajetsActifs(dateDebut, dateFin, placesMin)
                    .stream()
                    .filter(t -> {
                        if (departLat != null && departLon != null) {
                            BigDecimal tLat = t.getDepartLatitude();
                            BigDecimal tLon = t.getDepartLongitude();
                            if (tLat == null || tLon == null) return false;
                            if (haversineKm(departLat, departLon, tLat.doubleValue(), tLon.doubleValue()) > rayonKm) return false;
                        }
                        if (arriveeLat != null && arriveeLon != null) {
                            BigDecimal tLat = t.getArriveeLatitude();
                            BigDecimal tLon = t.getArriveeLongitude();
                            if (tLat == null || tLon == null) return false;
                            if (haversineKm(arriveeLat, arriveeLon, tLat.doubleValue(), tLon.doubleValue()) > rayonKm) return false;
                        }
                        return true;
                    })
                    .toList();
        } else {
            trajets = trajetRepository.searchTrajets(departVille, arriveeVille, dateDebut, dateFin, placesMin);
        }

        List<TrajetDetailResponse> responses = trajets.stream()
                .map(trajet -> new TrajetDetailResponse(
                        trajet.getId(),
                        trajet.getConducteur().getNom(),
                        trajet.getConducteur().getPrenom(),
                        trajet.getVoiture().getModele(),
                        trajet.getVoiture().getNbPassagers(),
                        trajet.getDepartRue(),
                        trajet.getDepartVille(),
                        trajet.getDepartDepartement(),
                        trajet.getDepartLatitude(),
                        trajet.getDepartLongitude(),
                        trajet.getArriveeRue(),
                        trajet.getArriveeVille(),
                        trajet.getArriveeDepartement(),
                        trajet.getArriveeLatitude(),
                        trajet.getArriveeLongitude(),
                        trajet.getHoraireDepart(),
                        trajet.getHoraireArrivee(),
                        trajet.getTempsTrajetMin(),
                        trajet.getPlacesDisponibles(),
                        trajet.getStatut()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }

    // GET /trajets/{id} — détail enrichi
    @GetMapping("/trajets/{id}")
    public ResponseEntity<TrajetDetailResponse> getTrajet(@PathVariable Integer id) {
        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));

        return ResponseEntity.ok(new TrajetDetailResponse(
                trajet.getId(),
                trajet.getConducteur().getNom(),
                trajet.getConducteur().getPrenom(),
                trajet.getVoiture().getModele(),
                trajet.getVoiture().getNbPassagers(),
                trajet.getDepartRue(),
                trajet.getDepartVille(),
                trajet.getDepartDepartement(),
                trajet.getDepartLatitude(),
                trajet.getDepartLongitude(),
                trajet.getArriveeRue(),
                trajet.getArriveeVille(),
                trajet.getArriveeDepartement(),
                trajet.getArriveeLatitude(),
                trajet.getArriveeLongitude(),
                trajet.getHoraireDepart(),
                trajet.getHoraireArrivee(),
                trajet.getTempsTrajetMin(),
                trajet.getPlacesDisponibles(),
                trajet.getStatut()
        ));
    }

    // POST /trajets — créer un trajet
    @Transactional
    @PostMapping("/trajets")
    public ResponseEntity<?> createTrajet(@RequestBody CreateTrajetRequest request) {
        Utilisateur connecte = utilisateurConnecte();

        if (!connecte.isPermisDeConduire()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Permis de conduire requis pour créer un trajet");
        }

        Voiture voiture = voitureRepository.findById(request.getVoitureId())
                .orElseThrow(() -> new IllegalArgumentException("Voiture introuvable"));

        if (!voiture.getUtilisateur().getId().equals(connecte.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getHoraireDepart() == null || request.getHoraireArrivee() == null
                || !request.getHoraireDepart().isBefore(request.getHoraireArrivee())) {
            return ResponseEntity.badRequest().body("L'horaire de depart doit etre avant l'horaire d'arrivee");
        }

        if (request.getPlacesDisponibles() == null
                || request.getPlacesDisponibles() > voiture.getNbPassagers()) {
            return ResponseEntity.badRequest()
                    .body("Le nombre de places disponibles ne peut pas depasser la capacite de la voiture");
        }

        Trajet trajet = new Trajet();
        trajet.setConducteur(connecte);
        trajet.setVoiture(voiture);
        trajet.setDepartRue(request.getDepartRue());
        trajet.setDepartVille(request.getDepartVille());
        trajet.setDepartDepartement(request.getDepartDepartement());
        trajet.setDepartLatitude(request.getDepartLatitude());
        trajet.setDepartLongitude(request.getDepartLongitude());
        trajet.setArriveeRue(request.getArriveeRue());
        trajet.setArriveeVille(request.getArriveeVille());
        trajet.setArriveeDepartement(request.getArriveeDepartement());
        trajet.setArriveeLatitude(request.getArriveeLatitude());
        trajet.setArriveeLongitude(request.getArriveeLongitude());
        trajet.setHoraireDepart(request.getHoraireDepart());
        trajet.setHoraireArrivee(request.getHoraireArrivee());
        trajet.setTempsTrajetMin(request.getTempsTrajetMin());
        trajet.setPlacesDisponibles(request.getPlacesDisponibles());
        trajet.setStatut(Trajet.Statut.actif);
        trajet.setCreatedAt(LocalDateTime.now());

        Trajet sauvegarde = trajetRepository.save(trajet);

        return ResponseEntity.status(HttpStatus.CREATED).body(new TrajetDetailResponse(
                sauvegarde.getId(),
                connecte.getNom(),
                connecte.getPrenom(),
                voiture.getModele(),
                voiture.getNbPassagers(),
                sauvegarde.getDepartRue(),
                sauvegarde.getDepartVille(),
                sauvegarde.getDepartDepartement(),
                sauvegarde.getDepartLatitude(),
                sauvegarde.getDepartLongitude(),
                sauvegarde.getArriveeRue(),
                sauvegarde.getArriveeVille(),
                sauvegarde.getArriveeDepartement(),
                sauvegarde.getArriveeLatitude(),
                sauvegarde.getArriveeLongitude(),
                sauvegarde.getHoraireDepart(),
                sauvegarde.getHoraireArrivee(),
                sauvegarde.getTempsTrajetMin(),
                sauvegarde.getPlacesDisponibles(),
                sauvegarde.getStatut()
        ));
    }

    // PATCH /trajets/{id} — modifier un trajet
    @PatchMapping("/trajets/{id}")
    public ResponseEntity<?> updateTrajet(
            @PathVariable Integer id,
            @RequestBody UpdateTrajetRequest request) {

        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!trajet.getConducteur().getId().equals(connecte.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (trajet.getStatut() != Trajet.Statut.actif) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (request.getDepartRue() != null) trajet.setDepartRue(request.getDepartRue());
        if (request.getDepartVille() != null) trajet.setDepartVille(request.getDepartVille());
        if (request.getDepartDepartement() != null) trajet.setDepartDepartement(request.getDepartDepartement());
        if (request.getDepartLatitude() != null) trajet.setDepartLatitude(request.getDepartLatitude());
        if (request.getDepartLongitude() != null) trajet.setDepartLongitude(request.getDepartLongitude());
        if (request.getArriveeRue() != null) trajet.setArriveeRue(request.getArriveeRue());
        if (request.getArriveeVille() != null) trajet.setArriveeVille(request.getArriveeVille());
        if (request.getArriveeDepartement() != null) trajet.setArriveeDepartement(request.getArriveeDepartement());
        if (request.getArriveeLatitude() != null) trajet.setArriveeLatitude(request.getArriveeLatitude());
        if (request.getArriveeLongitude() != null) trajet.setArriveeLongitude(request.getArriveeLongitude());
        if (request.getHoraireDepart() != null) trajet.setHoraireDepart(request.getHoraireDepart());
        if (request.getHoraireArrivee() != null) trajet.setHoraireArrivee(request.getHoraireArrivee());
        if (request.getTempsTrajetMin() != null) trajet.setTempsTrajetMin(request.getTempsTrajetMin());
        if (request.getPlacesDisponibles() != null) trajet.setPlacesDisponibles(request.getPlacesDisponibles());

        if (request.getVoitureId() != null) {
            Voiture voiture = voitureRepository.findById(request.getVoitureId())
                    .orElseThrow(() -> new IllegalArgumentException("Voiture introuvable"));
            if (!voiture.getUtilisateur().getId().equals(connecte.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            trajet.setVoiture(voiture);
        }

        // Validation post-PATCH : cohérence des horaires
        if (!trajet.getHoraireDepart().isBefore(trajet.getHoraireArrivee())) {
            return ResponseEntity.badRequest().body("L'horaire de depart doit etre avant l'horaire d'arrivee");
        }

        // Validation post-PATCH : places disponibles <= capacité voiture
        if (trajet.getPlacesDisponibles() > trajet.getVoiture().getNbPassagers()) {
            return ResponseEntity.badRequest()
                    .body("Le nombre de places disponibles ne peut pas depasser la capacite de la voiture");
        }

        Trajet sauvegarde = trajetRepository.save(trajet);

        return ResponseEntity.ok(new TrajetDetailResponse(
                sauvegarde.getId(),
                sauvegarde.getConducteur().getNom(),
                sauvegarde.getConducteur().getPrenom(),
                sauvegarde.getVoiture().getModele(),
                sauvegarde.getVoiture().getNbPassagers(),
                sauvegarde.getDepartRue(),
                sauvegarde.getDepartVille(),
                sauvegarde.getDepartDepartement(),
                sauvegarde.getDepartLatitude(),
                sauvegarde.getDepartLongitude(),
                sauvegarde.getArriveeRue(),
                sauvegarde.getArriveeVille(),
                sauvegarde.getArriveeDepartement(),
                sauvegarde.getArriveeLatitude(),
                sauvegarde.getArriveeLongitude(),
                sauvegarde.getHoraireDepart(),
                sauvegarde.getHoraireArrivee(),
                sauvegarde.getTempsTrajetMin(),
                sauvegarde.getPlacesDisponibles(),
                sauvegarde.getStatut()
        ));
    }

    // DELETE /trajets/{id} — annuler un trajet (soft delete + cascade reservations)
    @DeleteMapping("/trajets/{id}")
    public ResponseEntity<Void> deleteTrajet(@PathVariable Integer id) {
        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!trajet.getConducteur().getId().equals(connecte.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (trajet.getStatut() == Trajet.Statut.annule || trajet.getStatut() == Trajet.Statut.termine) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        trajet.setStatut(Trajet.Statut.annule);
        trajetRepository.save(trajet);

        List<Reservation> reservations = reservationRepository.findByTrajetId(id);
        for (Reservation reservation : reservations) {
            reservation.setStatut(Reservation.Statut.annule);
        }
        reservationRepository.saveAll(reservations);

        return ResponseEntity.noContent().build();
    }

    // PUT /trajets/{id}/terminer — terminer un trajet
    @PutMapping("/trajets/{id}/terminer")
    public ResponseEntity<?> terminerTrajet(@PathVariable Integer id) {
        Trajet trajet = trajetRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!trajet.getConducteur().getId().equals(connecte.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (trajet.getStatut() != Trajet.Statut.actif) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        trajet.setStatut(Trajet.Statut.termine);
        trajetRepository.save(trajet);

        return ResponseEntity.ok(new TrajetDetailResponse(
                trajet.getId(),
                trajet.getConducteur().getNom(),
                trajet.getConducteur().getPrenom(),
                trajet.getVoiture().getModele(),
                trajet.getVoiture().getNbPassagers(),
                trajet.getDepartRue(),
                trajet.getDepartVille(),
                trajet.getDepartDepartement(),
                trajet.getDepartLatitude(),
                trajet.getDepartLongitude(),
                trajet.getArriveeRue(),
                trajet.getArriveeVille(),
                trajet.getArriveeDepartement(),
                trajet.getArriveeLatitude(),
                trajet.getArriveeLongitude(),
                trajet.getHoraireDepart(),
                trajet.getHoraireArrivee(),
                trajet.getTempsTrajetMin(),
                trajet.getPlacesDisponibles(),
                trajet.getStatut()
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
