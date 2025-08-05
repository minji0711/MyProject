package com.example.mealkit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecentProductDto {
    private Long productId;
    private String productName;
}
