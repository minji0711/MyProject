package com.example.mealkit.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
public class OrderAdminDto {
	
	private Long orderId;
    private Long deliveryId;
    private LocalDateTime orderDate;
    private String memberName;
    private String productSummary;
    private int totalAmount;
    private String deliveryStatus;
    
}

