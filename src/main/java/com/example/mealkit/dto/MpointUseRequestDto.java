package com.example.mealkit.dto;

import lombok.*;

@Data
public class MpointUseRequestDto {
    private int usePoint;
    private Long orderId;  // 추가 수정
}
