package com.example.mealkit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private String orderDate;
    private String receiver;
    private String productImage;
    private String productSummary;
    private String totalAmount;
    private String deliveryStatus;
}

