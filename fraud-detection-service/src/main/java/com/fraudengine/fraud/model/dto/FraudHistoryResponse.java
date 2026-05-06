package com.fraudengine.fraud.model.dto;

import com.fraudengine.fraud.model.enums.FraudStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudHistoryResponse {
    private String transactionId;
    private BigDecimal amount;
    private Integer fraudScore;
    private FraudStatus status;
    private String triggeredRules;
    private Long cumulativeFraudCount;
    private LocalDateTime evaluatedAt;
}
