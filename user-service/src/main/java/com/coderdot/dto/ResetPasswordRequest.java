package com.coderdot.dto;

public class ResetPasswordRequest {
    private String newPassword;
    private String confirmPassword;

    // Constructeurs
    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String newPassword, String confirmPassword) {
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    // Getters et Setters
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
