package com.fraudengine.fraud.rule;

import com.fraudengine.fraud.model.dto.RuleResult;
import com.fraudengine.fraud.model.dto.TransactionContext;
import org.springframework.stereotype.Component;

@Component
public class RepeatOffenderRule implements FraudRule {

    @Override
    public RuleResult evaluate(TransactionContext ctx) {
        long fraudCount = ctx.getFraudCountHistory() != null ? ctx.getFraudCountHistory() : 0L;

        if (fraudCount >= 3) {
            return RuleResult.builder()
                    .ruleName(getRuleName())
                    .score(30)
                    .description(String.format("Account has %d fraud history records — high-risk account", fraudCount))
                    .build();
        }

        if (fraudCount >= 1) {
            return RuleResult.builder()
                    .ruleName(getRuleName())
                    .score(15)
                    .description(String.format("Account has %d previous fraud record(s)", fraudCount))
                    .build();
        }

        return RuleResult.builder()
                .ruleName(getRuleName())
                .score(0)
                .description("No fraud history found")
                .build();
    }

    @Override
    public String getRuleName() {
        return "REPEAT_OFFENDER";
    }
}
