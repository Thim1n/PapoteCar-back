package com.PapoteCar.PapoteCar.controller;

import com.PapoteCar.PapoteCar.dto.AuthResponse;
import com.PapoteCar.PapoteCar.dto.LoginRequest;
import com.PapoteCar.PapoteCar.dto.RegisterRequest;
import com.PapoteCar.PapoteCar.security.JwtUtil;
import com.PapoteCar.PapoteCar.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "Authentification — routes publiques (pas de token requis)")
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Operation(
            summary = "Créer un compte",
            description = "Crée un nouveau compte utilisateur. Retourne un token JWT valable jusqu'à minuit."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compte créé — token JWT retourné"),
            @ApiResponse(responseCode = "400", description = "Email déjà utilisé, username déjà pris, ou champ obligatoire manquant",
                    content = @Content(schema = @Schema(example = "{\"erreur\": \"Email déjà utilisé\"}")))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(
            summary = "Se connecter",
            description = "Connexion avec email ou username. Le champ `login` accepte indifféremment un email ou un username. Retourne un token JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie — token JWT retourné"),
            @ApiResponse(responseCode = "400", description = "Identifiants incorrects (utilisateur introuvable ou mot de passe erroné)",
                    content = @Content(schema = @Schema(example = "{\"erreur\": \"Mot de passe incorrect\"}")))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = {
                    @ExampleObject(name = "Connexion par email",
                            value = "{\"login\": \"alice@papotecar.fr\", \"motDePasse\": \"Test123\"}"),
                    @ExampleObject(name = "Connexion par username",
                            value = "{\"login\": \"alice\", \"motDePasse\": \"Test123\"}")
            })
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Se déconnecter",
            description = "Révoque le token JWT. Le token devient immédiatement invalide côté serveur.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Déconnecté avec succès"),
            @ApiResponse(responseCode = "401", description = "Token absent, mal formé ou déjà révoqué")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token invalide");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Token invalide");
        }
        jwtUtil.revokeToken(token);
        return ResponseEntity.ok("Déconnecté avec succès");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erreur", ex.getMessage()));
    }
}