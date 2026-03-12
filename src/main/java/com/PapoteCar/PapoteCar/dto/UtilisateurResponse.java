package com.PapoteCar.PapoteCar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UtilisateurResponse {
    private Integer id;
    private String nom;
    private String prenom;
    private String tel;
    private LocalDateTime createdAt;
    private String email; // null si profil public (pas son propre profil)
    private boolean permisDeConduire;
}