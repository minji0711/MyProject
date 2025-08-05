package com.example.mealkit.domain;

import java.time.LocalDate;


import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
public class Coupon {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String title;
	private String type;		//회원가입용 쿠폰:M, 기간쿠폰:T
	private int rate;
	private LocalDate useStart;
	private LocalDate useEnd;
	private LocalDateTime createdAt;
	
	@OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL)
	@JsonIgnore
    private List<Mcoupon> mcoupons;
	
	@PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

	public CouponStatus getStatus() {
	    LocalDate today = LocalDate.now();

	    if (useStart == null || useEnd == null) {
	        return CouponStatus.ALWAYS; 
	    }

	    if (today.isBefore(useStart)) {
	        return CouponStatus.NOTYET;
	    } else if (!today.isAfter(useEnd)) {
	        return CouponStatus.ACTIVE;
	    } else {
	        return CouponStatus.EXPIRED;
	    }
	}

}
