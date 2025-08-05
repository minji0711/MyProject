package com.example.mealkit.dto;

import lombok.Data;

@Data
public class TossConfirmRequestDto {
    private String paymentKey;
    private String orderId;
    private int amount;
}

