package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("ledger_entries")
public class LedgerEntry {

    @Id
    private Long id;

    private Long tutorId;
    private Long bookingId;

    private String type; // CREDIT | DEBIT | ADJUSTMENT

    private BigDecimal amount;
    private String description;

    private LocalDateTime createdAt;
}
