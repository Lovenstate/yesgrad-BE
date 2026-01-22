package com.yesgrad.service.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TutorCompleteSettingsRequest {
    // User fields
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(max = 20, message = "Phone number too long")
    private String phone;
    
    // TutorProfile fields
    @Size(max = 1000, message = "Bio must be less than 1000 characters")
    private String bio;
    
    private Boolean instantBook;
    
    // TutorSettings fields
    private Integer responseTime;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean lessonReminders;
    
    // Payment fields (future implementation)
    private String paymentMethod;
    private String payoutFrequency;
    
    // Password change (User)
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String currentPassword;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
    
    private String confirmPassword;
}
