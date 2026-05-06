package com.fraudengine.fraud.rule;

import com.fraudengine.fraud.model.dto.RuleResult;
import com.fraudengine.fraud.model.dto.TransactionContext;

public interface FraudRule {
    RuleResult evaluate(TransactionContext ctx);
    String getRuleName();
}
