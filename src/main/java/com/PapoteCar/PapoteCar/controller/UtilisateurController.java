package com.PapoteCar.PapoteCar.controller;

import com.PapoteCar.PapoteCar.dto.TrajetResponse;
import com.PapoteCar.PapoteCar.dto.UpdateUtilisateurRequest;
import com.PapoteCar.PapoteCar.dto.UtilisateurResponse;
import com.PapoteCar.PapoteCar.model.Trajet;
import com.PapoteCar.PapoteCar.model.Utilisateur;
import com.PapoteCar.PapoteCar.repository.TrajetRepository;
import com.PapoteCar.PapoteCar.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
                connecte.isPermisDeConduire()
        ));
    }

    @PostMapping("/me/permis")
    public ResponseEntity<UtilisateurResponse> uploadPermis(@RequestParam("fichier") MultipartFile fichier) {
        Utilisateur connecte = utilisateurConnecte();
        connecte.setPermisDeConduire(true);
        utilisateurRepository.save(connecte);
        return ResponseEntity.ok(new UtilisateurResponse(
                connecte.getId(),
                connecte.getNom(),
                connecte.getPrenom(),
                connecte.getTel(),
                connecte.getCreatedAt(),
                connecte.getEmail(),
                connecte.isPermisDeConduire()
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

        return ResponseEntity.ok(new UtilisateurResponse(
                cible.getId(),
                cible.getNom(),
                cible.getPrenom(),
                tel,
                cible.getCreatedAt(),
                email,
                cible.isPermisDeConduire()
        ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> updateUtilisateur(
            @PathVariable Integer id,
            @RequestBody UpdateUtilisateurRequest request) {

        Utilisateur connecte = utilisateurConnecte();
        if (!connecte.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getNom() != null) connecte.setNom(request.getNom());
        if (request.getPrenom() != null) connecte.setPrenom(request.getPrenom());
        if (request.getTel() != null) connecte.setTel(request.getTel());

        utilisateurRepository.save(connecte);

        return ResponseEntity.ok(new UtilisateurResponse(
                connecte.getId(),
                connecte.getNom(),
                connecte.getPrenom(),
                connecte.getTel(),
                connecte.getCreatedAt(),
                connecte.getEmail(),
                connecte.isPermisDeConduire()
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
                        trajet.getStatut(),
                        trajet.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(trajets);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Integer id) {
        Utilisateur connecte = utilisateurConnecte();
        if (!connecte.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (trajetRepository.existsByConducteurIdAndStatut(id, Trajet.Statut.actif)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        utilisateurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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