package com.fraudengine.fraud.controller;

import com.fraudengine.fraud.model.dto.FraudEvaluationRequest;
import com.fraudengine.fraud.model.dto.FraudEvaluationResponse;
import com.fraudengine.fraud.model.dto.FraudHistoryResponse;
import com.fraudengine.fraud.service.FraudDetectionService;
import com.fraudengine.fraud.service.FraudReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Tag(name = "Fraud Detection", description = "API for transaction fraud evaluation and monitoring")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;
    private final FraudReportService fraudReportService;

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate fraud for a transaction")
    public ResponseEntity<FraudEvaluationResponse> evaluate(
            @Valid @RequestBody FraudEvaluationRequest request) {
        return ResponseEntity.ok(fraudDetectionService.evaluate(request));
    }

    @GetMapping("/history/{accountId}")
    @Operation(summary = "Get fraud history for an account")
    public ResponseEntity<List<FraudHistoryResponse>> getFraudHistory(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(fraudDetectionService.getFraudHistory(accountId, limit));
    }

    @GetMapping("/report/daily")
    @Operation(summary = "Get daily fraud summary report")
    public ResponseEntity<List<Map<String, Object>>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(fraudReportService.getDailyFraudReport(startDate, endDate));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "fraud-detection-service",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
