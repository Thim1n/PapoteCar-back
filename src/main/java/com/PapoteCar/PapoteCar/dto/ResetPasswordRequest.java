package com.PapoteCar.PapoteCar.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String oldPassword;
    private String newPassword;
}