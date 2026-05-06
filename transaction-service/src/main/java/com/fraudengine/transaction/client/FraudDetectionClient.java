package com.fraudengine.transaction.client;

import com.fraudengine.transaction.model.dto.FraudEvaluationRequest;
import com.fraudengine.transaction.model.dto.FraudEvaluationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fraud-detection-service", url = "${fraud.service.url}")
public interface FraudDetectionClient {

    @PostMapping("/api/v1/fraud/evaluate")
    FraudEvaluationResponse evaluate(@RequestBody FraudEvaluationRequest request);
}
