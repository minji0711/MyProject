package com.example.mealkit.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
public class ReviewMypageDto {
	
	private Long orderId;
	private Long productId;
	private LocalDateTime orderDate;
	private String productName;
	private String productImage;
	private LocalDateTime reviewDate;
	private Long orderItemId;
	private int score;
	private String content;
	private String imagePath;
	private String imagePath2;
	private Long reviewId;
	
	public ReviewMypageDto(Long orderId, Long productId, LocalDateTime orderDate, String productName,
			String productImage, Long orderItemId) {
		this.orderId = orderId;
		this.productId = productId;
		this.orderDate = orderDate;
		this.productName = productName;
		this.productImage = productImage;
		this.orderItemId = orderItemId;
		
	}

	
	
	

}
