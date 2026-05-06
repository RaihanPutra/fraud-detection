package com.fraudengine.transaction.service;

import com.fraudengine.transaction.client.FraudDetectionClient;
import com.fraudengine.transaction.model.dto.*;
import com.fraudengine.transaction.model.entity.Transaction;
import com.fraudengine.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudDetectionClient fraudDetectionClient;

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Processing transaction {} for account {}", transactionId, request.getAccountId());

        FraudEvaluationRequest fraudRequest = FraudEvaluationRequest.builder()
                .transactionId(transactionId)
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .transactionTime(LocalDateTime.now())
                .build();

        FraudEvaluationResponse fraudResponse = fraudDetectionClient.evaluate(fraudRequest);

        String txStatus = "BLOCKED".equals(fraudResponse.getStatus()) ? "REJECTED" : "SUCCESS";

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .status(txStatus)
                .fraudScore(fraudResponse.getFraudScore())
                .fraudStatus(fraudResponse.getStatus())
                .build();

        transactionRepository.save(transaction);

        log.info("Transaction {} completed: status={}, fraudScore={}", transactionId, txStatus, fraudResponse.getFraudScore());

        return TransactionResponse.builder()
                .transactionId(transactionId)
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .status(txStatus)
                .fraudScore(fraudResponse.getFraudScore())
                .fraudStatus(fraudResponse.getStatus())
                .triggeredRules(fraudResponse.getTriggeredRules())
                .message(buildMessage(fraudResponse.getStatus(), fraudResponse.getFraudScore()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public List<TransactionResponse> getTransactionHistory(String accountId) {
        return transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(tx -> TransactionResponse.builder()
                        .transactionId(tx.getTransactionId())
                        .accountId(tx.getAccountId())
                        .amount(tx.getAmount())
                        .transactionType(tx.getTransactionType())
                        .status(tx.getStatus())
                        .fraudScore(tx.getFraudScore())
                        .fraudStatus(tx.getFraudStatus())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private String buildMessage(String fraudStatus, int score) {
        return switch (fraudStatus) {
            case "BLOCKED" -> String.format("Transaction rejected — fraud score %d is too high.", score);
            case "FLAGGED" -> String.format("Transaction processed but flagged as suspicious (score: %d). Please verify.", score);
            default -> String.format("Transaction successfully processed (fraud score: %d).", score);
        };
    }
}
