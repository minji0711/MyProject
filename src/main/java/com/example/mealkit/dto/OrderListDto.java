package com.example.mealkit.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
public class OrderListDto {
	
	private Long orderId;
    private LocalDateTime orderDate;
    private String productSummary;
    private int totalAmount;
    private String productImage;
    private Long deliveryId;
    private String receiver;
    private String deliveryStatus;
    
    // 추가
    private Long memberId;
    private int point;
   
    
    // 오버로딩 생성자 (조회에서 5개만 쓸 때 사용)
    public OrderListDto(Long orderId, LocalDateTime orderDate, String productSummary,
                        int totalAmount, String productImage) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.productSummary = productSummary;
        this.totalAmount = totalAmount;
        this.productImage = productImage;
    }
	    
}
