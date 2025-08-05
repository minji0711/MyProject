package com.example.mealkit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishResponseDto {

	private Long wishId;
    private Long productId;
    private String productName;
    private String productImage;
    private int price;
}
