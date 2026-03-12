package com.PapoteCar.PapoteCar.controller;

import com.PapoteCar.PapoteCar.dto.TrajetResponse;
import com.PapoteCar.PapoteCar.dto.UpdateUtilisateurRequest;
import com.PapoteCar.PapoteCar.dto.UtilisateurResponse;
import com.PapoteCar.PapoteCar.model.Trajet;
import com.PapoteCar.PapoteCar.model.Utilisateur;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Utilisateur", description = "Gestion du profil utilisateur")
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

    @Operation(summary = "Mon profil", description = "Retourne le profil complet de l'utilisateur connecté (id, nom, prenom, email, tel, solde, permisDeConduire, createdAt).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil retourné")
    })
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

    @Operation(
            summary = "Déposer son permis de conduire",
            description = """
                    Upload d'un fichier image du permis de conduire (form-data, champ `fichier`).
                    Passe `permisDeConduire` à `true` sans vérification (POC).
                    Requis pour pouvoir créer des trajets.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permis validé — profil mis à jour avec permisDeConduire=true")
    })
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

    @Operation(
            summary = "Profil d'un utilisateur par ID",
            description = """
                    Retourne le profil d'un utilisateur.
                    - **Propre profil** : tous les champs visibles (email, tel, solde)
                    - **Profil d'un autre** : email, tel et solde masqués (null)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil retourné"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable",
                    content = @Content(schema = @Schema(example = "Utilisateur introuvable")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> getUtilisateur(
            @Parameter(description = "ID de l'utilisateur", example = "1") @PathVariable Integer id) {
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

    @Operation(
            summary = "Modifier son profil",
            description = "Met à jour le profil de l'utilisateur connecté. Tous les champs sont optionnels (PATCH partiel). Seuls `nom`, `prenom` et `tel` sont modifiables."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil mis à jour"),
            @ApiResponse(responseCode = "403", description = "L'ID ne correspond pas à l'utilisateur connecté",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> updateUtilisateur(
            @Parameter(description = "ID de l'utilisateur (doit être le sien)", example = "1") @PathVariable Integer id,
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

    @Operation(
            summary = "Trajets d'un utilisateur (conducteur)",
            description = "Retourne la liste de tous les trajets créés par cet utilisateur en tant que conducteur (tous statuts confondus)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des trajets (vide si aucun)"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable",
                    content = @Content(schema = @Schema(example = "Utilisateur introuvable")))
    })
    @GetMapping("/{id}/trajets")
    public ResponseEntity<List<TrajetResponse>> getTrajetsUtilisateur(
            @Parameter(description = "ID de l'utilisateur", example = "1") @PathVariable Integer id) {
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

    @Operation(
            summary = "Supprimer son compte",
            description = "Supprime définitivement le compte. La suppression cascade sur les voitures et trajets associés."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compte supprimé"),
            @ApiResponse(responseCode = "403", description = "L'ID ne correspond pas à l'utilisateur connecté",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "L'utilisateur a des trajets actifs — annuler les trajets avant de supprimer le compte",
                    content = @Content(schema = @Schema(example = "Impossible de supprimer votre compte : vous avez un trajet actif en cours")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUtilisateur(
            @Parameter(description = "ID de l'utilisateur (doit être le sien)", example = "1") @PathVariable Integer id) {
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