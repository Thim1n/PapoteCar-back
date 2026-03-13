package com.PapoteCar.PapoteCar.service;

import com.PapoteCar.PapoteCar.dto.*;
import com.PapoteCar.PapoteCar.model.Utilisateur;
import com.PapoteCar.PapoteCar.repository.UtilisateurRepository;
import com.PapoteCar.PapoteCar.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            log.warn("Tentative d'inscription avec un email déjà utilisé : {}", request.getEmail());
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        if (utilisateurRepository.existsByUsername(request.getUsername())) {
            log.warn("Tentative d'inscription avec un username déjà utilisé : {}", request.getUsername());
            throw new IllegalArgumentException("Username déjà utilisé");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setUsername(request.getUsername());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setTel(request.getTel());
        utilisateurRepository.save(utilisateur);
        log.info("Nouvel utilisateur enregistré : email={}, username={}", utilisateur.getEmail(), utilisateur.getUsername());

        String token = jwtUtil.generateToken(utilisateur.getEmail());
        return new AuthResponse(token, expireLabel());
    }

    public AuthResponse login(LoginRequest request) {
        String login = request.getLogin();

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Identifiant requis");
        }

        Utilisateur utilisateur = (login.contains("@")
                ? utilisateurRepository.findByEmail(login)
                : utilisateurRepository.findByUsername(login))
                .orElseThrow(() -> {
                    log.warn("Tentative de connexion avec un identifiant inconnu : {}", login);
                    return new IllegalArgumentException("Utilisateur introuvable : " + login);
                });

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            log.warn("Mot de passe incorrect pour l'identifiant : {}", login);
            throw new IllegalArgumentException("Mot de passe incorrect pour : " + login);
        }

        String token = jwtUtil.generateToken(utilisateur.getEmail());
        log.info("Connexion réussie pour : {}", login);
        return new AuthResponse(token, expireLabel());
    }

    public void forgotPassword(ForgotPasswordRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));

        utilisateurRepository.save(utilisateur);
    }

    public void resetPassword(ResetPasswordRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getOldPassword(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(utilisateur);
    }



    private LocalDateTime expireLabel() {
        return LocalDate.now(ZoneId.of("Europe/Paris"))
                .plusDays(1)
                .atStartOfDay();
    }
}