package com.example.mealkit.dto;

import org.springframework.web.multipart.MultipartFile;
import lombok.*;

@Data
@AllArgsConstructor
public class ReviewResponseDto {
	
	private int score;
	private String content;
	private MultipartFile  imagePath1;
	private MultipartFile  imagePath2;
    private Long orderItemId;
	private Long memberId;
	private Long productId;

}
