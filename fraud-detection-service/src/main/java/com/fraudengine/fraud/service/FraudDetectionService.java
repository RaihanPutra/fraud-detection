package com.fraudengine.fraud.service;

import com.fraudengine.fraud.model.dto.*;
import com.fraudengine.fraud.model.entity.FraudEvaluation;
import com.fraudengine.fraud.model.enums.FraudStatus;
import com.fraudengine.fraud.repository.FraudEvaluationRepository;
import com.fraudengine.fraud.rule.FraudRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FraudDetectionService {

    private final List<FraudRule> fraudRules;
    private final FraudEvaluationRepository repository;

    @Value("${fraud.threshold.blocked:70}")
    private int thresholdBlocked;

    @Value("${fraud.threshold.flagged:40}")
    private int thresholdFlagged;

    @Autowired
    public FraudDetectionService(List<FraudRule> fraudRules,
                                  FraudEvaluationRepository repository) {
        this.fraudRules = fraudRules;
        this.repository = repository;
        log.info("FraudDetectionService initialized with {} rules: {}",
                fraudRules.size(),
                fraudRules.stream().map(FraudRule::getRuleName).collect(Collectors.joining(", ")));
    }

    @Transactional
    public FraudEvaluationResponse evaluate(FraudEvaluationRequest request) {
        log.info("Evaluating transaction: {}", request.getTransactionId());

        if (request.getTransactionTime() == null) {
            request.setTransactionTime(LocalDateTime.now());
        }

        TransactionContext ctx = buildContext(request);

        List<RuleResult> ruleResults = fraudRules.stream()
                .map(rule -> rule.evaluate(ctx))
                .collect(Collectors.toList());

        int totalScore = Math.min(
                ruleResults.stream()
                        .mapToInt(RuleResult::getScore)
                        .sum(),
                100
        );

        List<String> triggeredRules = ruleResults.stream()
                .filter(r -> r.getScore() > 0)
                .map(r -> String.format("%s(+%d): %s", r.getRuleName(), r.getScore(), r.getDescription()))
                .collect(Collectors.toList());

        FraudStatus status = determineStatus(totalScore);
        String recommendation = buildRecommendation(status);

        FraudEvaluation evaluation = FraudEvaluation.builder()
                .transactionId(request.getTransactionId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .fraudScore(totalScore)
                .status(status)
                .triggeredRules(String.join(" | ", triggeredRules))
                .recommendation(recommendation)
                .build();
        repository.save(evaluation);

        log.info("Transaction {} evaluated: score={}, status={}", request.getTransactionId(), totalScore, status);

        return FraudEvaluationResponse.builder()
                .transactionId(request.getTransactionId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .fraudScore(totalScore)
                .status(status)
                .triggeredRules(triggeredRules)
                .recommendation(recommendation)
                .evaluatedAt(LocalDateTime.now())
                .build();
    }

    public List<FraudHistoryResponse> getFraudHistory(String accountId, int limit) {
        List<Object[]> rows = repository.getFraudHistoryWithCumulativeCount(accountId, limit);

        return rows.stream()
                .map(row -> FraudHistoryResponse.builder()
                        .transactionId((String) row[0])
                        .amount((BigDecimal) row[1])
                        .fraudScore(((Number) row[2]).intValue())
                        .status(FraudStatus.valueOf((String) row[3]))
                        .triggeredRules((String) row[4])
                        .evaluatedAt(((java.sql.Timestamp) row[5]).toLocalDateTime())
                        .cumulativeFraudCount(((Number) row[6]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private TransactionContext buildContext(FraudEvaluationRequest request) {
        LocalDateTime oneHourAgo = request.getTransactionTime().minusHours(1);

        Long txCountLastHour = repository.countRecentTransactions(request.getAccountId(), oneHourAgo);
        Double avgAmount = repository.getAverageAmountLast30Days(request.getAccountId());
        Long fraudCount = repository.countFraudHistory(request.getAccountId());

        return TransactionContext.builder()
                .transactionId(request.getTransactionId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .transactionTime(request.getTransactionTime())
                .txCountLastHour(txCountLastHour)
                .avgAmountLast30Days(avgAmount != null ? BigDecimal.valueOf(avgAmount) : BigDecimal.ZERO)
                .fraudCountHistory(fraudCount)
                .build();
    }

    private FraudStatus determineStatus(int score) {
        if (score >= thresholdBlocked) return FraudStatus.BLOCKED;
        if (score >= thresholdFlagged) return FraudStatus.FLAGGED;
        return FraudStatus.APPROVED;
    }

    private String buildRecommendation(FraudStatus status) {
        return switch (status) {
            case BLOCKED -> "Transaction automatically blocked. Contact fraud team for investigation.";
            case FLAGGED -> "Transaction flagged as suspicious. Manual verification required.";
            case APPROVED -> "Transaction approved. No fraud indicators detected.";
        };
    }
}
