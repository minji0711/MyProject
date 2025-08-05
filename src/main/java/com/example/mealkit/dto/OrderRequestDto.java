package com.example.mealkit.dto;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
public class OrderRequestDto {
	
	List<OrderItemDto> orderItemDtos;
	private Long destinationId;
	private int deliveryFee;
	private String deliveryRequest;
	private int usedPoint;
	private Long mcouponId;
	private int discountCoupon;
	private String paymentMethod;
	private int payment;
	private int amount;
	
}
