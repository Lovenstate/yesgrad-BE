package com.yesgrad.service.domain;

import lombok.Data;

@Data
public class TutorSettingsRequest {

    private Integer responseTime;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean lessonReminders;
}