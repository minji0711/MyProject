package com.example.mealkit.repository;

import java.awt.print.Pageable;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mealkit.domain.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
			
	// 쿠폰 제목으로 조회
    List<Coupon> findByTitleContaining(String title);
    
    // 특정 기간에 발행된 쿠폰 조회
    List<Coupon> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // 회원가입 쿠폰 조회
    Coupon findTop1ByType(String type);
    
    // 상품 상세페이지 > 오늘 날짜 기준 사용 가능한 쿠폰 조회 (Type T:기간, M:회원가입)
    List<Coupon> findTop2ByTypeAndUseStartLessThanEqualAndUseEndGreaterThanEqualOrderByIdAsc(String Type, LocalDate today1, LocalDate today2);    
    
    
}
