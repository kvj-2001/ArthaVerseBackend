package com.billing.config;

import com.billing.entity.User;
import com.billing.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        createDefaultAdminUser();
    }
    
    private void createDefaultAdminUser() {
        // Check if admin user already exists
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@billing.app");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFirstName("System");
            adminUser.setLastName("Administrator");
            adminUser.setRole(User.Role.ADMIN);
            adminUser.setStatus(User.UserStatus.APPROVED);
            adminUser.setEmailVerified(true);
            
            userRepository.save(adminUser);
            logger.info("Default admin user created successfully with username: admin");
        } else {
            // Update existing admin user to ensure it's enabled
            User existingAdmin = userRepository.findByUsername("admin").orElse(null);
            if (existingAdmin != null && existingAdmin.getStatus() != User.UserStatus.APPROVED) {
                existingAdmin.setStatus(User.UserStatus.APPROVED);
                existingAdmin.setEmailVerified(true);
                userRepository.save(existingAdmin);
                logger.info("Updated existing admin user status to APPROVED");
            } else {
                logger.info("Default admin user already exists and is properly configured");
            }
        }
    }
}
