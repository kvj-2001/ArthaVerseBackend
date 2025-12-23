package com.billing.service;

import com.billing.dto.UserRegistrationDto;
import com.billing.dto.UserResponseDto;
import com.billing.entity.User;
import com.billing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private ModelMapper modelMapper;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private AuthService authService;
    
    private UserRegistrationDto registrationDto;
    private User user;
    
    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");
        
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
    }
    
    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.existsByUsername(registrationDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        UserResponseDto result = authService.registerUser(registrationDto);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(User.class));
    }
    
    @Test
    void registerUser_UsernameAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(registrationDto.getUsername())).thenReturn(true);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> authService.registerUser(registrationDto));
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(registrationDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> authService.registerUser(registrationDto));
        
        verify(userRepository, never()).save(any(User.class));
    }
}
