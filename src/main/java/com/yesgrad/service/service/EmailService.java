package com.yesgrad.service.service;

import com.yesgrad.service.config.PropertiesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final PropertiesConfig config;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.brevo.com/v3")
            .build();

    public Mono<Void> sendEmail(
            String templateType,
            String email,
            String firstName,
            Map<String, Object> params
    ) {
        log.info("Sending email to: {}", email);

        String htmlContent = buildHtmlTemplate(templateType, firstName, params);
        String subject = getSubject(templateType);

        Map<String, Object> request = Map.of(
                "sender", Map.of(
                        "email", config.getBrevo().getSender().getEmail(),
                        "name", config.getBrevo().getSender().getName()
                ),
                "to", List.of(Map.of(
                        "email", email,
                        "name", firstName
                )),
                "subject", subject,
                "htmlContent", htmlContent
        );

        return webClient.post()
                .uri("/smtp/email")
                .header("api-key", config.getBrevo().getApiKey())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class)
                                .doOnNext(body ->
                                        log.info("Email sent to {} – response: {}", email, body)
                                )
                                .then();
                    }
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("<no-body>")
                            .flatMap(body -> {
                                log.error(
                                        "Failed to send email to {} – status={} body={}",
                                        email, response.statusCode(), body
                                );
                                return Mono.error(
                                        new RuntimeException("Brevo error: " + body)
                                );
                            });
                })
                .doOnError(e ->
                        log.error("sendEmail error for {}: {}", email, e.getMessage(), e)
                );
    }


    private String buildHtmlTemplate(String type, String firstName, Map<String, Object> params) {
        return switch (type) {
            case "account-registration" -> buildRegistrationTemplate(firstName, params);
            case "account-activation" -> buildActivationTemplate(firstName, params);
            case "email-verification" -> buildEmailVerificationTemplate(firstName, params);
            case "password-reset" -> buildPasswordResetTemplate(firstName, params);
            case "password-reset-success" -> buildPasswordResetSuccessTemplate(firstName, params);
            default -> throw new IllegalArgumentException("Unknown template type: " + type);
        };
    }

    private String getSubject(String type) {
        return switch (type) {
            case "account-registration" -> "Welcome to YesGrad!";
            case "account-activation" -> "Activate Your YesGrad Account";
            case "email-verification" -> "Verify Your Email Address";
            case "password-reset" -> "Reset Your Password";
            case "password-reset-success" -> "Password Reset Successful";
            default -> "YesGrad Notification";
        };
    }

    private String buildRegistrationTemplate(String firstName, Map<String, Object> params) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Welcome to YesGrad, %s!</h2>
                <p>Thank you for registering with us.</p>
                <p>We're excited to have you on board.</p>
                <p>Best regards,<br>The YesGrad Team</p>
            </body>
            </html>
            """.formatted(firstName);
    }

    private String buildActivationTemplate(String firstName, Map<String, Object> params) {
        String activationLink = (String) params.get("activationLink");
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Activate Your Account</h2>
                <p>Hi %s,</p>
                <p>Please click the link below to activate your account:</p>
                <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Activate Account</a></p>
                <p>This link expires in 24 hours.</p>
                <p>Best regards,<br>The YesGrad Team</p>
            </body>
            </html>
            """.formatted(firstName, activationLink);
    }

    private String buildEmailVerificationTemplate(String firstName, Map<String, Object> params) {
        String verificationLink = (String) params.get("verificationLink");
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Verify Your Email</h2>
                <p>Hi %s,</p>
                <p>Thank you for registering! Please verify your email address to continue:</p>
                <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Verify Email</a></p>
                <p>This link expires in 24 hours.</p>
                <p>If you didn't create an account, please ignore this email.</p>
                <p>Best regards,<br>The YesGrad Team</p>
            </body>
            </html>
            """.formatted(firstName, verificationLink);
    }

    private String buildPasswordResetTemplate(String firstName, Map<String, Object> params) {
        String resetLink = (String) params.get("resetLink");
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Reset Your Password</h2>
                <p>Hi %s,</p>
                <p>Click the link below to reset your password:</p>
                <p><a href="%s" style="background-color: #2196F3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a></p>
                <p>This link expires in 30 minutes.</p>
                <p>If you didn't request this, please ignore this email.</p>
                <p>Best regards,<br>The YesGrad Team</p>
            </body>
            </html>
            """.formatted(firstName, resetLink);
    }
    
    private String buildPasswordResetSuccessTemplate(String firstName, Map<String, Object> params) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Password Reset Successful</h2>
                <p>Hi %s,</p>
                <p>Your password has been successfully reset.</p>
                <p>If you didn't make this change, please contact us immediately.</p>
                <p>Best regards,<br>The YesGrad Team</p>
            </body>
            </html>
            """.formatted(firstName);
    }
}
