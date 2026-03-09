package com.PapoteCar.PapoteCar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String expireA; // "minuit le YYYY-MM-DD"
}