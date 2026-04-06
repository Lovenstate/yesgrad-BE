package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("tutor_settings")
public class TutorSettings {

    @Id
    private Long id;
    private Long userId;
    private Integer responseTime;
    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean lessonReminders;
}
