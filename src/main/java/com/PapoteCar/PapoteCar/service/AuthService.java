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

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setTel(request.getTel());
        utilisateurRepository.save(utilisateur);

        String token = jwtUtil.generateToken(utilisateur.getEmail());
        return new AuthResponse(token, expireLabel());
    }

    public AuthResponse login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        String token = jwtUtil.generateToken(utilisateur.getEmail());
        return new AuthResponse(token, expireLabel());
    }

    private String expireLabel() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Europe/Paris")).plusDays(1);
        return "Minuit le " + tomorrow;
    }
}