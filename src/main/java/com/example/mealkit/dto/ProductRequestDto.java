package com.example.mealkit.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProductRequestDto {
    private String name;
    private String description;
    private int price;
    private String prdDetail;
    //private String prdInfo;
    private String category;
    private String category2;

    private MultipartFile image;
    private MultipartFile image2; // 서브 이미지
}
