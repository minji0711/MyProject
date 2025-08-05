package com.example.mealkit.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaDetailResponseDto {

	private String category;
	private String title;
	private String content;
	private String answer;
	private boolean answered;
	private String imagePath;
	private LocalDateTime createdAt;
}
