package com.fraudengine.fraud.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResult {
    private String ruleName;
    private int score;
    private String description;
}
