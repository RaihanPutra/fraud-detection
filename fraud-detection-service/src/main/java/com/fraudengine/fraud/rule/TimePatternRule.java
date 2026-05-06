package com.fraudengine.fraud.rule;

import com.fraudengine.fraud.model.dto.RuleResult;
import com.fraudengine.fraud.model.dto.TransactionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TimePatternRule implements FraudRule {

    @Value("${fraud.rules.time-pattern.suspicious-start-hour:0}")
    private int suspiciousStartHour;

    @Value("${fraud.rules.time-pattern.suspicious-end-hour:4}")
    private int suspiciousEndHour;

    @Value("${fraud.rules.time-pattern.score:25}")
    private int suspiciousScore;

    @Override
    public RuleResult evaluate(TransactionContext ctx) {
        int hour = ctx.getTransactionTime().getHour();

        if (hour >= suspiciousStartHour && hour < suspiciousEndHour) {
            return RuleResult.builder()
                    .ruleName(getRuleName())
                    .score(suspiciousScore)
                    .description(String.format("Transaction at %02d:xx — outside normal active hours (00:00-04:00)", hour))
                    .build();
        }

        return RuleResult.builder()
                .ruleName(getRuleName())
                .score(0)
                .description(String.format("Transaction time is normal (%02d:xx)", hour))
                .build();
    }

    @Override
    public String getRuleName() {
        return "TIME_PATTERN";
    }
}
