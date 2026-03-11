package com.PapoteCar.PapoteCar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private LocalDateTime expireA; // "minuit le YYYY-MM-DD"
}