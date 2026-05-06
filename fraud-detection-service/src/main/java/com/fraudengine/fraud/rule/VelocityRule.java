package com.fraudengine.fraud.rule;

import com.fraudengine.fraud.model.dto.RuleResult;
import com.fraudengine.fraud.model.dto.TransactionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VelocityRule implements FraudRule {

    @Value("${fraud.rules.velocity.max-tx-per-hour:5}")
    private int maxTxPerHour;

    @Value("${fraud.rules.velocity.score-high:40}")
    private int scoreHigh;

    @Value("${fraud.rules.velocity.score-medium:20}")
    private int scoreMedium;

    @Override
    public RuleResult evaluate(TransactionContext ctx) {
        long count = ctx.getTxCountLastHour() != null ? ctx.getTxCountLastHour() : 0L;

        if (count > maxTxPerHour * 2) {
            return RuleResult.builder()
                    .ruleName(getRuleName())
                    .score(scoreHigh)
                    .description(String.format("Transaction count very high: %d times in 1 hour (normal limit: %d)", count, maxTxPerHour))
                    .build();
        }

        if (count > maxTxPerHour) {
            return RuleResult.builder()
                    .ruleName(getRuleName())
                    .score(scoreMedium)
                    .description(String.format("Transaction count above normal: %d times in 1 hour", count))
                    .build();
        }

        return RuleResult.builder()
                .ruleName(getRuleName())
                .score(0)
                .description("Transaction frequency is normal")
                .build();
    }

    @Override
    public String getRuleName() {
        return "VELOCITY_CHECK";
    }
}
