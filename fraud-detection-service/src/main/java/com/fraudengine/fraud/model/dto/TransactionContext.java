package com.fraudengine.fraud.model.dto;

import com.fraudengine.fraud.model.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionContext {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private LocalDateTime transactionTime;
    private Long txCountLastHour;
    private BigDecimal avgAmountLast30Days;
    private Long fraudCountHistory;
}
