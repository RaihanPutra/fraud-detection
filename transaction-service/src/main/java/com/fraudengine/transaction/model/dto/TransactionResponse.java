package com.fraudengine.transaction.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private Integer fraudScore;
    private String fraudStatus;
    private List<String> triggeredRules;
    private String message;
    private LocalDateTime createdAt;
}
