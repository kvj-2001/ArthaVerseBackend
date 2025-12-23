package com.billing.dto;

public class PasswordResetResponseDto {
    private String newPassword;
    
    public PasswordResetResponseDto() {}
    
    public PasswordResetResponseDto(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
