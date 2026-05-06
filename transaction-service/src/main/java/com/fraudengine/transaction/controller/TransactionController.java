package com.fraudengine.transaction.controller;

import com.fraudengine.transaction.model.dto.TransactionRequest;
import com.fraudengine.transaction.model.dto.TransactionResponse;
import com.fraudengine.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "API for processing banking transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Process a new transaction")
    public ResponseEntity<TransactionResponse> processTransaction(
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.processTransaction(request));
    }

    @GetMapping("/history/{accountId}")
    @Operation(summary = "Get transaction history for an account")
    public ResponseEntity<List<TransactionResponse>> getHistory(@PathVariable String accountId) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountId));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "transaction-service",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
