package com.example.mealkit.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaResponseDto {
	
	private Long id;
	private String category;
	private String title;
	private LocalDateTime createdAt;
	private boolean answered;
	private String memberName;
}
