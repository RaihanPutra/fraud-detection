package com.fraudengine.transaction.repository;

import com.fraudengine.transaction.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(String accountId);

    @Query(value = """
            SELECT
                account_id,
                COUNT(*)       AS total_transactions,
                SUM(amount)    AS total_amount,
                AVG(amount)    AS avg_amount,
                COUNT(CASE WHEN fraud_status IN ('FLAGGED','BLOCKED') THEN 1 END) AS fraud_count
            FROM transactions
            WHERE account_id = :accountId
            GROUP BY account_id
            """, nativeQuery = true)
    Object[] getAccountSummary(@Param("accountId") String accountId);
}
