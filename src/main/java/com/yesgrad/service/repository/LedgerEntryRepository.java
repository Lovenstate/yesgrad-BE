package com.yesgrad.service.repository;

import com.yesgrad.service.domain.LedgerEntry;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface LedgerEntryRepository extends ReactiveCrudRepository<LedgerEntry, Long> {

    Flux<LedgerEntry> findByTutorId(Long tutorId);

    @Query("""
            SELECT COALESCE(SUM(amount), 0)
            FROM ledger_entries
            WHERE tutor_id = :tutorId
            """)
    Mono<BigDecimal> getTutorBalance(Long tutorId);
}
