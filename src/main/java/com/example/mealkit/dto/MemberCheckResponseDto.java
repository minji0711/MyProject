package com.example.mealkit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberCheckResponseDto {

	private boolean exists;
	private String message;
}
