package com.yesgrad.service.domain;

import java.math.BigDecimal;

public record EarningsSummary(
        BigDecimal availableBalance,
        BigDecimal weekEarnings,
        BigDecimal monthEarnings
) {
}
