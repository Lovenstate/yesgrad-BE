package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("tutor_payout_settings")
public class TutorPayoutSettings {

    @Id
    private Long id;

    private Long tutorId;

    private String payoutMethod; // STRIPE | ACH
    private String externalAccountId;

    private String schedule; // WEEKLY | BIWEEKLY | MONTHLY

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
