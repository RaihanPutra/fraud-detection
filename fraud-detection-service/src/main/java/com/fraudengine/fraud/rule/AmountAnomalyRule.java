package com.fraudengine.fraud.rule;

import com.fraudengine.fraud.model.dto.RuleResult;
import com.fraudengine.fraud.model.dto.TransactionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AmountAnomalyRule implements FraudRule {

    @Value("${fraud.rules.amount-anomaly.ratio-critical:10.0}")
    private double ratioCritical;

    @Value("${fraud.rules.amount-anomaly.ratio-high:5.0}")
    private double ratioHigh;

    @Value("${fraud.rules.amount-anomaly.score-critical:35}")
    private int scoreCritical;

    @Value("${fraud.rules.amount-anomaly.score-high:20}")
    private int scoreHigh;

    @Override
    public RuleResult evaluate(TransactionContext ctx) {
        BigDecimal avgAmount = ctx.getAvgAmountLast30Days();

        if (avgAmount == null || avgAmount.compareTo(BigDecimal.ZERO) == 0) {
            return RuleResult.builder()
                    .ruleName(getRuleName())
                    .score(0)
                    .description("No historical baseline available")
                    .build();
        }

        double ratio = ctx.getAmount()
                .divide(avgAmount, 2, RoundingMode.HALF_UP)
                .doubleValue();

        if (ratio >= ratioCritical) {
            return RuleResult.builder()
                    .ruleName(getRuleName())
                    .score(scoreCritical)
                    .description(String.format("Amount is %.1fx above historical average (critical anomaly)", ratio))
                    .build();
        }

        if (ratio >= ratioHigh) {
            return RuleResult.builder()
                    .ruleName(getRuleName())
                    .score(scoreHigh)
                    .description(String.format("Amount is %.1fx above historical average", ratio))
                    .build();
        }

        return RuleResult.builder()
                .ruleName(getRuleName())
                .score(0)
                .description(String.format("Transaction amount is normal (%.1fx average)", ratio))
                .build();
    }

    @Override
    public String getRuleName() {
        return "AMOUNT_ANOMALY";
    }
}
