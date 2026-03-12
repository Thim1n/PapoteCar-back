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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Voiture", description = "Gestion des voitures — ownership vérifié sur toutes les routes")
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

    @Operation(summary = "Mes voitures", description = "Retourne la liste des voitures appartenant à l'utilisateur connecté.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des voitures (vide si aucune)")
    })
    @GetMapping("/voitures")
    public ResponseEntity<List<VoitureResponse>> getMesVoitures() {
        Utilisateur connecte = utilisateurConnecte();
        List<VoitureResponse> voitures = voitureRepository.findByUtilisateurId(connecte.getId())
                .stream()
                .map(v -> new VoitureResponse(v.getId(), v.getModele(), v.getNbPassagers(), v.getCouleur(), v.getTailleCoffre()))
                .toList();
        return ResponseEntity.ok(voitures);
    }

    @Operation(summary = "Détail d'une voiture", description = "Retourne le détail d'une voiture. Accès réservé au propriétaire.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voiture retournée"),
            @ApiResponse(responseCode = "403", description = "L'utilisateur connecté n'est pas propriétaire de cette voiture",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Voiture introuvable",
                    content = @Content(schema = @Schema(example = "Voiture introuvable")))
    })
    @GetMapping("/voiture/{id}")
    public ResponseEntity<VoitureResponse> getVoiture(
            @Parameter(description = "ID de la voiture", example = "1") @PathVariable Integer id) {
        Voiture voiture = voitureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voiture introuvable"));

        Utilisateur connecte = utilisateurConnecte();
        if (!voitureRepository.existsByIdAndUtilisateurId(id, connecte.getId())) {
            log.warn("Accès interdit : utilisateur id={} tente d'accéder à la voiture id={}", connecte.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(new VoitureResponse(voiture.getId(), voiture.getModele(), voiture.getNbPassagers(), voiture.getCouleur(), voiture.getTailleCoffre()));
    }

    @Operation(
            summary = "Créer une voiture",
            description = """
                    Crée une nouvelle voiture pour l'utilisateur connecté.

                    **Champs :**
                    - `modele` (obligatoire) — ex: "Renault Zoé"
                    - `nbPassagers` (obligatoire, ≥ 1) — capacité maximale, utilisée pour valider `placesDisponibles` lors de la création d'un trajet
                    - `couleur` (optionnel) — ex: "Blanche"
                    - `tailleCoffre` (optionnel) — `Petit`, `Moyen` ou `Grand`
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Voiture créée"),
            @ApiResponse(responseCode = "400", description = "Modèle manquant ou nbPassagers invalide",
                    content = @Content(schema = @Schema(example = "Le modele est obligatoire")))
    })
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

    @Operation(
            summary = "Modifier une voiture",
            description = """
                    Met à jour une voiture. Tous les champs sont optionnels (PATCH partiel).

                    **Garde métier :** impossible de modifier une voiture utilisée dans un trajet actif (409).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voiture mise à jour"),
            @ApiResponse(responseCode = "400", description = "Modèle vide ou nbPassagers invalide",
                    content = @Content(schema = @Schema(example = "Le modele ne peut pas etre vide"))),
            @ApiResponse(responseCode = "403", description = "L'utilisateur connecté n'est pas propriétaire",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Voiture introuvable",
                    content = @Content(schema = @Schema(example = "Voiture introuvable"))),
            @ApiResponse(responseCode = "409", description = "La voiture est utilisée dans un trajet actif",
                    content = @Content(schema = @Schema(example = "Impossible de modifier cette voiture : un trajet actif l'utilise actuellement")))
    })
    @PatchMapping("/voiture/{id}")
    public ResponseEntity<?> updateVoiture(
            @Parameter(description = "ID de la voiture", example = "1") @PathVariable Integer id,
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

    @Operation(
            summary = "Supprimer une voiture",
            description = """
                    Supprime définitivement une voiture.

                    **Garde métier :** impossible de supprimer une voiture utilisée dans un trajet actif (409).
                    Terminer ou annuler le trajet avant de supprimer la voiture.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Voiture supprimée"),
            @ApiResponse(responseCode = "403", description = "L'utilisateur connecté n'est pas propriétaire",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Voiture introuvable",
                    content = @Content(schema = @Schema(example = "Voiture introuvable"))),
            @ApiResponse(responseCode = "409", description = "La voiture est utilisée dans un trajet actif",
                    content = @Content(schema = @Schema(example = "Impossible de supprimer cette voiture : un trajet actif l'utilise actuellement")))
    })
    @DeleteMapping("/voiture/{id}")
    public ResponseEntity<?> deleteVoiture(
            @Parameter(description = "ID de la voiture", example = "1") @PathVariable Integer id) {
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