package com.PapoteCar.PapoteCar.controller;

import com.PapoteCar.PapoteCar.dto.TrajetResponse;
import com.PapoteCar.PapoteCar.dto.UpdateUtilisateurRequest;
import com.PapoteCar.PapoteCar.dto.UtilisateurResponse;
import com.PapoteCar.PapoteCar.model.Trajet;
import com.PapoteCar.PapoteCar.model.Utilisateur;
import com.PapoteCar.PapoteCar.repository.TrajetRepository;
import com.PapoteCar.PapoteCar.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;

    private Utilisateur utilisateurConnecte() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }

    @GetMapping("/me")
    public ResponseEntity<UtilisateurResponse> getMe() {
        Utilisateur connecte = utilisateurConnecte();
        return ResponseEntity.ok(new UtilisateurResponse(
                connecte.getId(),
                connecte.getNom(),
                connecte.getPrenom(),
                connecte.getTel(),
                connecte.getCreatedAt(),
                connecte.getEmail(),
                connecte.isPermisDeConduire(),
                connecte.getSolde()
        ));
    }

    @PostMapping("/me/permis")
    public ResponseEntity<UtilisateurResponse> uploadPermis(@RequestParam("fichier") MultipartFile fichier) {
        Utilisateur connecte = utilisateurConnecte();
        connecte.setPermisDeConduire(true);
        utilisateurRepository.save(connecte);
        log.info("Permis de conduire validé pour utilisateur id={}", connecte.getId());
        return ResponseEntity.ok(new UtilisateurResponse(
                connecte.getId(),
                connecte.getNom(),
                connecte.getPrenom(),
                connecte.getTel(),
                connecte.getCreatedAt(),
                connecte.getEmail(),
                connecte.isPermisDeConduire(),
                connecte.getSolde()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> getUtilisateur(@PathVariable Integer id) {
        Utilisateur cible = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        boolean estProprietaire = connecte.getId().equals(id);
        String email = estProprietaire ? cible.getEmail() : null;
        String tel   = estProprietaire ? cible.getTel()   : null;

        BigDecimal solde = estProprietaire ? cible.getSolde() : null;
        return ResponseEntity.ok(new UtilisateurResponse(
                cible.getId(),
                cible.getNom(),
                cible.getPrenom(),
                tel,
                cible.getCreatedAt(),
                email,
                cible.isPermisDeConduire(),
                solde
        ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> updateUtilisateur(
            @PathVariable Integer id,
            @RequestBody UpdateUtilisateurRequest request) {

        Utilisateur connecte = utilisateurConnecte();
        if (!connecte.getId().equals(id)) {
            log.warn("Accès interdit : utilisateur id={} tente de modifier le profil id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getNom() != null) connecte.setNom(request.getNom());
        if (request.getPrenom() != null) connecte.setPrenom(request.getPrenom());
        if (request.getTel() != null) connecte.setTel(request.getTel());

        utilisateurRepository.save(connecte);
        log.info("Profil mis à jour pour utilisateur id={}", connecte.getId());

        return ResponseEntity.ok(new UtilisateurResponse(
                connecte.getId(),
                connecte.getNom(),
                connecte.getPrenom(),
                connecte.getTel(),
                connecte.getCreatedAt(),
                connecte.getEmail(),
                connecte.isPermisDeConduire(),
                connecte.getSolde()
        ));
    }

    @GetMapping("/{id}/trajets")
    public ResponseEntity<List<TrajetResponse>> getTrajetsUtilisateur(@PathVariable Integer id) {
        utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        List<TrajetResponse> trajets = trajetRepository.findByConducteurId(id).stream()
                .map(trajet -> new TrajetResponse(
                        trajet.getId(),
                        trajet.getDepartVille(),
                        trajet.getArriveeVille(),
                        trajet.getHoraireDepart(),
                        trajet.getHoraireArrivee(),
                        trajet.getPlacesDisponibles(),
                        trajet.getPrix(),
                        trajet.getStatut(),
                        trajet.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(trajets);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUtilisateur(@PathVariable Integer id) {
        Utilisateur connecte = utilisateurConnecte();
        if (!connecte.getId().equals(id)) {
            log.warn("Accès interdit : utilisateur id={} tente de supprimer le compte id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (trajetRepository.existsByConducteurIdAndStatut(id, Trajet.Statut.actif)) {
            log.warn("Conflit suppression compte id={} : trajet actif en cours", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de supprimer votre compte : vous avez un trajet actif en cours");
        }

        utilisateurRepository.deleteById(id);
        log.info("Compte supprimé id={}", id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        log.error("Erreur inattendue dans UtilisateurController : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }


}