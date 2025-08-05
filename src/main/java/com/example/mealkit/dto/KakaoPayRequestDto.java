package com.example.mealkit.dto;

import lombok.Data;

@Data
public class KakaoPayRequestDto {
    private String orderId;
    private String userId;
    private String itemName;
    private int amount;
}
