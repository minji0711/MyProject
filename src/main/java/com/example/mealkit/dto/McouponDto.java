package com.example.mealkit.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
public class McouponDto {
	
	private Long couponId;
	private Long memberId;
	private String couponTitle;
	private int couponRate;
	private LocalDate useStart;			//회원가입일에 따라 사용기간이 달라짐.(for person)
	private LocalDate useEnd;
	private String useYn;				// used:Y, N, P
	private LocalDateTime usedAt;

}
