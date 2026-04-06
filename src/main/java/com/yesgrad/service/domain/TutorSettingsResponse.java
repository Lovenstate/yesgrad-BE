package com.yesgrad.service.domain;

import lombok.Data;

@Data
public class TutorSettingsResponse {
    private Long id;
    private Long userId;
    private Integer responseTime;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean lessonReminders;
}
