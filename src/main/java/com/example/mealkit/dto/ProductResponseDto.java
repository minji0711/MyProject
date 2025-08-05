package com.example.mealkit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private int price;
    private String imagePath;
    private String imagePath2;
    private String prdDetail;
    private String prdInfo;
    private String category;
    private String category2;

}
