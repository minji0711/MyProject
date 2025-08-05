package com.example.mealkit.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class OrderItemViewDto {
	
	private Long productId;
	private String productName;
	private int productPrice;
	private String productImage;
	private int productQuantity;
}
