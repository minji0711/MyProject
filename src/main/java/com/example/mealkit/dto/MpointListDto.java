package com.example.mealkit.dto;

import java.time.LocalDateTime;


import lombok.*;

@Data
@AllArgsConstructor
public class MpointListDto {
	
	private LocalDateTime createdAt;	
	private String status;			// 적립 , 사용
	private String description;		// 리뷰작성 포인트 적립, 주문금액 5% 적립, 상품구매 포인트 차감
	private String point;

}
