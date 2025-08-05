package com.example.mealkit.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class OrderViewDto {
	
	private int amount;
	private int discountCoupon;
	private int point;
	private int deliveryFee;
	private int payment;
	private String paymentMethod;
	private String receiver;
	private String receiverPhone;
	private String receiverAddress;
	private String deliveryRequest;
	
}
