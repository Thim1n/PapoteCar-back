package com.PapoteCar.PapoteCar.controller;

import com.PapoteCar.PapoteCar.dto.CreateVoitureRequest;
import com.PapoteCar.PapoteCar.dto.UpdateVoitureRequest;
import com.PapoteCar.PapoteCar.dto.VoitureResponse;
import com.PapoteCar.PapoteCar.model.Trajet;
import com.PapoteCar.PapoteCar.model.Utilisateur;
import com.PapoteCar.PapoteCar.model.Voiture;
import com.PapoteCar.PapoteCar.repository.TrajetRepository;
import com.PapoteCar.PapoteCar.repository.UtilisateurRepository;
import com.PapoteCar.PapoteCar.repository.VoitureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VoitureController {

    private final VoitureRepository voitureRepository;
    private final TrajetRepository trajetRepository;
    private final UtilisateurRepository utilisateurRepository;

    private Utilisateur utilisateurConnecte() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }

    // GET /voitures — liste les voitures du connecté
    @GetMapping("/voitures")
    public ResponseEntity<List<VoitureResponse>> getMesVoitures() {
        Utilisateur connecte = utilisateurConnecte();
        List<VoitureResponse> voitures = voitureRepository.findByUtilisateurId(connecte.getId())
                .stream()
                .map(v -> new VoitureResponse(v.getId(), v.getModele(), v.getNbPassagers(), v.getCouleur(), v.getTailleCoffre()))
                .toList();
        return ResponseEntity.ok(voitures);
    }

    // GET /voiture/{id} — détail d'une voiture (ownership)
    @GetMapping("/voiture/{id}")
    public ResponseEntity<VoitureResponse> getVoiture(@PathVariable Integer id) {
        Voiture voiture = voitureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voiture introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!voitureRepository.existsByIdAndUtilisateurId(id, connecte.getId())) {
            log.warn("Accès interdit : utilisateur id={} tente d'accéder à la voiture id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(new VoitureResponse(voiture.getId(), voiture.getModele(), voiture.getNbPassagers(), voiture.getCouleur(), voiture.getTailleCoffre()));
    }

    // POST /voitures — créer une voiture
    @PostMapping("/voitures")
    public ResponseEntity<?> createVoiture(@RequestBody CreateVoitureRequest request) {
        if (request.getModele() == null || request.getModele().isBlank()) {
            return ResponseEntity.badRequest().body("Le modele est obligatoire");
        }
        if (request.getNbPassagers() == null || request.getNbPassagers() < 1) {
            return ResponseEntity.badRequest().body("Le nombre de passagers doit etre au moins 1");
        }

        Utilisateur connecte = utilisateurConnecte();

        Voiture voiture = new Voiture();
        voiture.setUtilisateur(connecte);
        voiture.setModele(request.getModele());
        voiture.setNbPassagers(request.getNbPassagers());
        voiture.setCouleur(request.getCouleur());
        voiture.setTailleCoffre(request.getTailleCoffre());

        Voiture sauvegardee = voitureRepository.save(voiture);
        log.info("Voiture créée id={} pour utilisateur id={}", sauvegardee.getId(), connecte.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new VoitureResponse(sauvegardee.getId(), sauvegardee.getModele(), sauvegardee.getNbPassagers(), sauvegardee.getCouleur(), sauvegardee.getTailleCoffre()));
    }

    // PATCH /voiture/{id} — modifier une voiture (ownership + guard trajet actif)
    @PatchMapping("/voiture/{id}")
    public ResponseEntity<?> updateVoiture(
            @PathVariable Integer id,
            @RequestBody UpdateVoitureRequest request) {

        Voiture voiture = voitureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voiture introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!voitureRepository.existsByIdAndUtilisateurId(id, connecte.getId())) {
            log.warn("Accès interdit : utilisateur id={} ne possède pas la voiture id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (trajetRepository.existsByVoitureIdAndStatut(id, Trajet.Statut.actif)) {
            log.warn("Conflit modification voiture id={} : trajet actif en cours", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de modifier cette voiture : un trajet actif l'utilise actuellement");
        }

        if (request.getModele() != null && request.getModele().isBlank()) {
            return ResponseEntity.badRequest().body("Le modele ne peut pas etre vide");
        }
        if (request.getNbPassagers() != null && request.getNbPassagers() < 1) {
            return ResponseEntity.badRequest().body("Le nombre de passagers doit etre au moins 1");
        }

        if (request.getModele() != null) voiture.setModele(request.getModele());
        if (request.getNbPassagers() != null) voiture.setNbPassagers(request.getNbPassagers());
        if (request.getCouleur() != null) voiture.setCouleur(request.getCouleur());
        if (request.getTailleCoffre() != null) voiture.setTailleCoffre(request.getTailleCoffre());

        voitureRepository.save(voiture);
        log.info("Voiture mise à jour id={} par utilisateur id={}", id, connecte.getId());

        return ResponseEntity.ok(new VoitureResponse(voiture.getId(), voiture.getModele(), voiture.getNbPassagers(), voiture.getCouleur(), voiture.getTailleCoffre()));
    }

    // DELETE /voiture/{id} — supprimer une voiture (ownership + guard trajet actif)
    @DeleteMapping("/voiture/{id}")
    public ResponseEntity<?> deleteVoiture(@PathVariable Integer id) {
        voitureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voiture introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!voitureRepository.existsByIdAndUtilisateurId(id, connecte.getId())) {
            log.warn("Accès interdit : utilisateur id={} ne possède pas la voiture id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (trajetRepository.existsByVoitureIdAndStatut(id, Trajet.Statut.actif)) {
            log.warn("Conflit suppression voiture id={} : trajet actif en cours", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de supprimer cette voiture : un trajet actif l'utilise actuellement");
        }

        voitureRepository.deleteById(id);
        log.info("Voiture supprimée id={} par utilisateur id={}", id, connecte.getId());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        log.error("Erreur inattendue dans VoitureController : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
