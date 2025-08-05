package com.example.mealkit.domain;


import java.time.LocalDate;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class Mcoupon {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate useStart;
	private LocalDate useEnd;
	private LocalDateTime usedAt;
	private LocalDateTime createdAt;
	
	@Column(length = 1)
	private String useYn;		// used:Y, N, P
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
	@JsonIgnoreProperties({"mcoupons"})
    private Member member;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orders_id", unique = true)
	private Orders orders;
	
	
	@PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
	
	
	//쿠폰 기간만료 처리(useYn = P)
	public boolean isExpired(LocalDate now) {
		return useEnd != null && useEnd.isBefore(now) && !"Y".equals(useYn);
	}
		
}
