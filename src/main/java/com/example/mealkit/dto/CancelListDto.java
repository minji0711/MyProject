package com.example.mealkit.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor
public class CancelListDto {
    private Long orderId;
    private LocalDateTime orderDate;
    private String productSummary;
    private int totalAmount;
    private String productImage;
}
