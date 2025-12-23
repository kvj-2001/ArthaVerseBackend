package com.billing.controller;

import com.billing.dto.JwtResponseDto;
import com.billing.dto.LoginDto;
import com.billing.dto.PasswordResetResponseDto;
import com.billing.dto.UserRegistrationDto;
import com.billing.dto.UserResponseDto;
import com.billing.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserResponseDto user = authService.registerUser(registrationDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Authenticate user and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        JwtResponseDto response = authService.authenticateUser(loginDto);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Verify user email")
    @GetMapping("/verify-email")
    public ResponseEntity<UserResponseDto> verifyEmail(@RequestParam String token) {
        UserResponseDto user = authService.verifyEmail(token);
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "Get pending users (Admin only)")
    @GetMapping("/pending-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getPendingUsers() {
        List<UserResponseDto> users = authService.getPendingUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(summary = "Approve user (Admin only)")
    @PutMapping("/approve-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> approveUser(@PathVariable Long userId) {
        UserResponseDto user = authService.approveUser(userId);
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "Reject user (Admin only)")
    @PutMapping("/reject-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> rejectUser(@PathVariable Long userId) {
        UserResponseDto user = authService.rejectUser(userId);
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "Get all users (Admin only)")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(summary = "Create user (Admin only)")
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserResponseDto user = authService.createUser(registrationDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Update user (Admin only)")
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId, @RequestBody UserRegistrationDto userDto) {
        UserResponseDto user = authService.updateUser(userId, userDto);
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "Delete user (Admin only)")
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Update user status (Admin only)")
    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUserStatus(@PathVariable Long userId, @RequestParam boolean enabled) {
        UserResponseDto user = authService.updateUserStatus(userId, enabled);
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "Reset user password (Admin only)")
    @PostMapping("/users/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PasswordResetResponseDto> resetUserPassword(@PathVariable Long userId) {
        String newPassword = authService.resetUserPassword(userId);
        return ResponseEntity.ok(new PasswordResetResponseDto(newPassword));
    }
}
