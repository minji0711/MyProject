package com.example.mealkit.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDeleteRequestDto {

	private List<Long> cartItemIds;

}
