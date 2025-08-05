package com.example.mealkit.dto;
import lombok.*;

@Data
@AllArgsConstructor
public class CouponListDto {
	
	private Long couponId;
	private String type;   //회원가입용 쿠폰:M, 기간쿠폰:T
	private String title;
	private int rate;
	private String period;
	private String status;

}


