package com.yesgrad.service.domain;

import lombok.Data;

@Data
public class TutorCompleteSettingsResponse {
    // User fields
    private String email;
    private String phone;
    
    // TutorProfile fields
    private String bio;
    private Boolean instantBook;
    
    // TutorSettings fields
    private String responseTime;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean lessonReminders;
    
    // Payment fields
    private String paymentMethod;
    private String payoutFrequency;
    
    // Password change flag
    private Boolean passwordChanged;
}
