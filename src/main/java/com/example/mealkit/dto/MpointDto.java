package com.example.mealkit.dto;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
public class MpointDto {
	
	private List<Long> memberIds;
	private int point;
	private String description;

}
