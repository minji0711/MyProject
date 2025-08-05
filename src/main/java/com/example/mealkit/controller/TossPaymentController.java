package com.example.mealkit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.example.mealkit.dto.TossConfirmRequestDto;

@RestController
@RequestMapping("/toss-payments")
@RequiredArgsConstructor
public class TossPaymentController {

    @Value("${toss.secret-key}")  // properties에서 toss.secret-key로 읽어옴
    private String tossSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 결제 승인 요청 API
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody TossConfirmRequestDto request) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";

        // 토스는 Basic Auth를 요구합니다.
        String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 구성
        Map<String, String> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", request.getOrderId());
        body.put("amount", String.valueOf(request.getAmount()));

        // HTTP 요청
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 승인 실패");
        }
    }

    // 테스트용 ping API (서버 살아있는지 확인용)
    @GetMapping("/ping")
    public String ping() {
        return "Toss Payment Controller is working!";
    }
}
