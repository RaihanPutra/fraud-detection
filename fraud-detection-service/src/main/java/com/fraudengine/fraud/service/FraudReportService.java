package com.fraudengine.fraud.service;

import com.fraudengine.fraud.repository.FraudEvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudReportService {

    private final FraudEvaluationRepository repository;

    public List<Map<String, Object>> getDailyFraudReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rows = repository.getDailyFraudSummary(startDate, endDate);

        return rows.stream()
                .map(row -> {
                    Map<String, Object> report = new LinkedHashMap<>();
                    report.put("reportDate", row[0].toString());
                    report.put("totalTransactions", ((Number) row[1]).longValue());
                    report.put("flaggedCount", ((Number) row[2]).longValue());
                    report.put("blockedCount", ((Number) row[3]).longValue());
                    report.put("avgFraudScore", row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);
                    report.put("totalAtRiskAmount", row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO);

                    long flagged = ((Number) row[2]).longValue();
                    long blocked = ((Number) row[3]).longValue();
                    long total = ((Number) row[1]).longValue();
                    double fraudRate = total > 0
                            ? Math.round(((double) (flagged + blocked) / total) * 10000.0) / 100.0
                            : 0.0;
                    report.put("fraudRatePercent", fraudRate);

                    return report;
                })
                .collect(Collectors.toList());
    }
}
