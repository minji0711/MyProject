package com.example.mealkit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import com.example.mealkit.dto.KakaoPayRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
public class KakaoPayController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/ready")
    public ResponseEntity<?> kakaoPayReady(@RequestBody KakaoPayRequestDto dto) {
    	
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK fb4e54f7ee6b8060373b6017061d7476"); // 실제 키로 교체
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", "TC0ONETIME"); // 테스트용 CID
        body.add("partner_order_id", dto.getOrderId());
        body.add("partner_user_id", dto.getUserId());
        body.add("item_name", dto.getItemName());
        body.add("quantity", "1");
        body.add("total_amount", String.valueOf(dto.getAmount()));
        body.add("tax_free_amount", "0");
        body.add("approval_url", "http://localhost:3000/kakao/success");
        body.add("cancel_url", "http://localhost:3000/kakao/cancel");
        body.add("fail_url", "http://localhost:3000/kakao/fail");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v1/payment/ready", request, Map.class);

        // tid와 redirect URL을 프론트에 넘김
        return ResponseEntity.ok(response.getBody());
    }
}

