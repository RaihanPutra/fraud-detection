package com.fraudengine.transaction.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudEvaluationRequest {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String transactionType;
    private LocalDateTime transactionTime;
}
