package com.fraudengine.fraud.repository;

import com.fraudengine.fraud.model.entity.FraudEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudEvaluationRepository extends JpaRepository<FraudEvaluation, Long> {

    Optional<FraudEvaluation> findByTransactionId(String transactionId);

    @Query(value = """
            SELECT COUNT(*)
            FROM fraud_evaluations
            WHERE account_id = :accountId
              AND evaluated_at >= :since
              AND status != 'BLOCKED'
            """, nativeQuery = true)
    Long countRecentTransactions(@Param("accountId") String accountId,
                                 @Param("since") LocalDateTime since);

    @Query(value = """
            SELECT COALESCE(AVG(amount), 0)
            FROM fraud_evaluations
            WHERE account_id = :accountId
              AND evaluated_at >= NOW() - INTERVAL '30 days'
              AND status != 'BLOCKED'
            """, nativeQuery = true)
    Double getAverageAmountLast30Days(@Param("accountId") String accountId);

    @Query(value = """
            SELECT COUNT(*)
            FROM fraud_evaluations
            WHERE account_id = :accountId
              AND status IN ('FLAGGED', 'BLOCKED')
            """, nativeQuery = true)
    Long countFraudHistory(@Param("accountId") String accountId);

    @Query(value = """
            SELECT
                fe.transaction_id,
                fe.amount,
                fe.fraud_score,
                fe.status,
                fe.triggered_rules,
                fe.evaluated_at,
                COUNT(fe.id) OVER (
                    PARTITION BY fe.account_id
                    ORDER BY fe.evaluated_at
                    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                ) AS cumulative_fraud_count
            FROM fraud_evaluations fe
            WHERE fe.account_id = :accountId
              AND fe.status IN ('FLAGGED', 'BLOCKED')
            ORDER BY fe.evaluated_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> getFraudHistoryWithCumulativeCount(@Param("accountId") String accountId,
                                                       @Param("limit") int limit);

    @Query(value = """
            SELECT
                DATE_TRUNC('day', evaluated_at)    AS report_date,
                COUNT(*)                           AS total_transactions,
                COUNT(CASE WHEN status = 'FLAGGED'  THEN 1 END) AS flagged_count,
                COUNT(CASE WHEN status = 'BLOCKED'  THEN 1 END) AS blocked_count,
                ROUND(AVG(fraud_score), 2)         AS avg_fraud_score,
                ROUND(SUM(CASE WHEN status IN ('FLAGGED','BLOCKED') THEN amount ELSE 0 END), 2)
                                                   AS total_at_risk_amount
            FROM fraud_evaluations
            WHERE evaluated_at BETWEEN :startDate AND :endDate
            GROUP BY DATE_TRUNC('day', evaluated_at)
            ORDER BY report_date DESC
            """, nativeQuery = true)
    List<Object[]> getDailyFraudSummary(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}
