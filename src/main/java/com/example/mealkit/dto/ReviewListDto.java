package com.example.mealkit.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
public class ReviewListDto {
	

	private Long reviewId;
    private String userName;
    private String userEmail;
    private LocalDateTime orderDate;
    private Long productId;
    private String productName;
    private int score;
    private String content;
    private LocalDateTime createdAt;
    private String imagePath;
    private String imagePath2;


}
