package com.billing.service;

import com.billing.dto.JwtResponseDto;
import com.billing.dto.LoginDto;
import com.billing.dto.UserRegistrationDto;
import com.billing.dto.UserResponseDto;
import com.billing.entity.User;
import com.billing.exception.BadRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.repository.UserRepository;
import com.billing.security.JwtTokenProvider;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private EmailService emailService;
    
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setRole(User.Role.USER);
        user.setStatus(User.UserStatus.PENDING);
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        
        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser);
        
        logger.info("User registered successfully: {}", savedUser.getUsername());
        
        return new UserResponseDto(savedUser);
    }
    
    public JwtResponseDto authenticateUser(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(),
                loginDto.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = tokenProvider.generateToken(loginDto.getUsername());
        
        User user = userRepository.findByUsername(loginDto.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", loginDto.getUsername()));
        
        UserResponseDto userResponse = new UserResponseDto(user);
        
        return new JwtResponseDto(jwt, userResponse);
    }
    
    public UserResponseDto verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new BadRequestException("Invalid verification token"));
        
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        
        User savedUser = userRepository.save(user);
        
        logger.info("Email verified for user: {}", savedUser.getUsername());
        
        return new UserResponseDto(savedUser);
    }
    
    public List<UserResponseDto> getPendingUsers() {
        List<User> pendingUsers = userRepository.findByStatus(User.UserStatus.PENDING);
        return pendingUsers.stream()
            .map(UserResponseDto::new)
            .collect(Collectors.toList());
    }
    
    public UserResponseDto approveUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (!user.isEmailVerified()) {
            throw new BadRequestException("User email must be verified before approval");
        }
        
        user.setStatus(User.UserStatus.APPROVED);
        
        // Create user-specific database name
        String databaseName = "billing_user_" + userId;
        user.setDatabaseName(databaseName);
        
        User savedUser = userRepository.save(user);
        
        // TODO: Create user-specific database
        // This would involve creating a new database schema for the user
        
        logger.info("User approved: {}", savedUser.getUsername());
        
        return new UserResponseDto(savedUser);
    }
    
    public UserResponseDto rejectUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setStatus(User.UserStatus.REJECTED);
        User savedUser = userRepository.save(user);
        
        logger.info("User rejected: {}", savedUser.getUsername());
        
        return new UserResponseDto(savedUser);
    }
    
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(UserResponseDto::new)
            .collect(Collectors.toList());
    }
    
    public UserResponseDto createUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        
        // Set role from DTO or default to USER
        if (registrationDto.getRole() != null && registrationDto.getRole().equalsIgnoreCase("ADMIN")) {
            user.setRole(User.Role.ADMIN);
        } else {
            user.setRole(User.Role.USER);
        }
        
        // Set status based on enabled field or default to APPROVED for admin-created users
        if (registrationDto.getEnabled() != null && !registrationDto.getEnabled()) {
            user.setStatus(User.UserStatus.SUSPENDED);
        } else {
            user.setStatus(User.UserStatus.APPROVED);
        }
        
        user.setEmailVerified(true); // Admin created users are auto-verified
        
        User savedUser = userRepository.save(user);
        
        logger.info("User created by admin: {}", savedUser.getUsername());
        
        return new UserResponseDto(savedUser);
    }
    
    public UserResponseDto updateUser(Long userId, UserRegistrationDto userDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check if username is being changed and if it conflicts
        if (!user.getUsername().equals(userDto.getUsername()) && 
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        
        // Check if email is being changed and if it conflicts
        if (!user.getEmail().equals(userDto.getEmail()) && 
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }
        
        // Update user fields
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        
        // Update role if provided
        if (userDto.getRole() != null) {
            if (userDto.getRole().equalsIgnoreCase("ADMIN")) {
                user.setRole(User.Role.ADMIN);
            } else {
                user.setRole(User.Role.USER);
            }
        }
        
        // Update status based on enabled field
        if (userDto.getEnabled() != null) {
            if (userDto.getEnabled()) {
                user.setStatus(User.UserStatus.APPROVED);
            } else {
                user.setStatus(User.UserStatus.SUSPENDED);
            }
        }
        
        // Only update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        User savedUser = userRepository.save(user);
        
        logger.info("User updated by admin: {}", savedUser.getUsername());
        
        return new UserResponseDto(savedUser);
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        userRepository.delete(user);
        
        logger.info("User deleted by admin: {}", user.getUsername());
    }
    
    public UserResponseDto updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        User.UserStatus newStatus = enabled ? User.UserStatus.APPROVED : User.UserStatus.SUSPENDED;
        user.setStatus(newStatus);
        
        User savedUser = userRepository.save(user);
        
        logger.info("User status updated by admin: {} - {}", savedUser.getUsername(), newStatus);
        
        return new UserResponseDto(savedUser);
    }
    
    public String resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Generate a random temporary password
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(newPassword));
        
        userRepository.save(user);
        
        logger.info("Password reset by admin for user: {}", user.getUsername());
        
        return newPassword;
    }
}
