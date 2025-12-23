package com.billing.service;

import com.billing.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${server.port}")
    private String serverPort;
    
    public void sendVerificationEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Email Verification - Billing Application");
            
            String verificationUrl = "http://localhost:" + serverPort + "/api/auth/verify-email?token=" + user.getEmailVerificationToken();
            
            String messageText = String.format(
                "Dear %s,\n\n" +
                "Please click the following link to verify your email address:\n\n" +
                "%s\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Billing Application Team",
                user.getFirstName(),
                verificationUrl
            );
            
            message.setText(messageText);
            emailSender.send(message);
            
            logger.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }
    
    public void sendApprovalNotification(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Account Approved - Billing Application");
            
            String messageText = String.format(
                "Dear %s,\n\n" +
                "Congratulations! Your account has been approved.\n\n" +
                "You can now log in to the billing application and start using all features.\n\n" +
                "Best regards,\n" +
                "Billing Application Team",
                user.getFirstName()
            );
            
            message.setText(messageText);
            emailSender.send(message);
            
            logger.info("Approval notification sent to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send approval notification to: {}", user.getEmail(), e);
        }
    }
    
    public void sendInvoiceEmail(String toEmail, String customerName, String invoiceNumber, byte[] pdfData) {
        // TODO: Implement invoice email with PDF attachment
        logger.info("Invoice email would be sent to: {} for invoice: {}", toEmail, invoiceNumber);
    }
}
