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
public class FraudEvaluationResponse {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private Integer fraudScore;
    private String status;
    private List<String> triggeredRules;
    private String recommendation;
    private LocalDateTime evaluatedAt;
}
