package com.billing.controller;

import com.billing.dto.UserRegistrationDto;
import com.billing.dto.UserResponseDto;
import com.billing.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser
    void registerUser_Success() throws Exception {
        // Given
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");
        
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setUsername("testuser");
        responseDto.setEmail("test@example.com");
        
        when(authService.registerUser(any(UserRegistrationDto.class))).thenReturn(responseDto);
        
        // When & Then
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
    
    @Test
    @WithMockUser
    void registerUser_InvalidInput_BadRequest() throws Exception {
        // Given
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        // Missing required fields
        
        // When & Then
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }
}
