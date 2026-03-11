package com.PapoteCar.PapoteCar.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class LoginRequest {
    @JsonAlias({"email", "username"})
    private String login; // accepte "login", "email" ou "username" en JSON
    private String motDePasse;
}