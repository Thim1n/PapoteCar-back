package com.PapoteCar.PapoteCar.service;

import com.PapoteCar.PapoteCar.dto.AuthResponse;
import com.PapoteCar.PapoteCar.dto.LoginRequest;
import com.PapoteCar.PapoteCar.dto.RegisterRequest;
import com.PapoteCar.PapoteCar.model.Utilisateur;
import com.PapoteCar.PapoteCar.repository.UtilisateurRepository;
import com.PapoteCar.PapoteCar.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        if (utilisateurRepository.existsByUsername(request.getUsername())) {
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
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + login));

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Mot de passe incorrect pour : " + login);
        }

        String token = jwtUtil.generateToken(utilisateur.getEmail());
        return new AuthResponse(token, expireLabel());
    }



    private LocalDateTime expireLabel() {
        return LocalDate.now(ZoneId.of("Europe/Paris"))
                .plusDays(1)
                .atStartOfDay();
    }
}