package com.yesgrad.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EmailService {

    public Mono<Void> sendEmail(String email, String firstName, String token) {

        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        System.out.println("\n" + "=".repeat(80));
        System.out.println("PASSWORD RESET EMAIL");
        System.out.println("=".repeat(80));
        System.out.println("To: " + email);
        System.out.println("Subject: Reset Your Password");
        System.out.println("\nHi " + firstName + ",");
        System.out.println("\nClick the link below to reset your password:");
        System.out.println(resetLink);
        System.out.println("\nThis link expires in 30 minutes.");
        System.out.println("\nToken: " + token);
        System.out.println("=".repeat(80) + "\n");

        log.info("Password reset email sent to: {}", email);
        log.info("Reset token: {}", token);
        log.info("Reset link: {}", resetLink);

        return Mono.empty();
    }
}
