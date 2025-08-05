package com.example.mealkit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaRequestDto {
    private String category;
    private String title;
    private String content;
    private Long orderItemId;
    private Long productId;
    //추가
    private String answer;
}

