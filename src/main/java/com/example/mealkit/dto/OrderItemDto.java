package com.example.mealkit.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class OrderItemDto {
	
	private Long productId ;
	private int quantity;
	private int price;

}
