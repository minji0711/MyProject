package com.example.mealkit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponseDto {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String imagePath;
    private int quantity;
    private int price; // 개당 가격
    private int totalPrice; // 수량 * 가격
}

