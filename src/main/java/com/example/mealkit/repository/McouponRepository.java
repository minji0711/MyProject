package com.example.mealkit.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.mealkit.domain.Mcoupon;
import com.example.mealkit.domain.Member;

public interface McouponRepository extends JpaRepository<Mcoupon, Long>{
	
	// 회원 쿠폰 전체 조회
	List<Mcoupon> findByMemberId(Long memberId);
	
	// 회원 사용, 미사용 쿠폰 조회 (UseYn Y:사용완료, N:미사용, P:기간만료
	List<Mcoupon> findByMemberIdAndUseYn(Long memberId, String useYn);
	
	// 사용가능한 쿠폰갯수 조회
	int countByMemberIdAndUseYn(Long memberId, String useYn);
	
	// 다운 받은 쿠폰여부 체크
	Optional<Mcoupon> findByMemberIdAndCouponId(Long memberId, Long couponId);
	
	// 상품 주문시 쿠폰여부 체크
	Optional<Mcoupon> findByMemberIdAndId(Long memberId, Long couponId);
	
	//회원 가입 축하쿠폰 존재여부 체크
	boolean existsByMemberAndCoupon_Type(Member member, String type);
	
	
	//추가
	// 주문 시 쿠폰 조회 (couponId = Coupon 엔티티의 id 기준)
    Optional<Mcoupon> findByMemberIdAndCoupon_Id(Long memberId, Long couponId);
    
    // orders_id로 직접 조회하는 안전 조회 추가
    Mcoupon findByOrders_Id(Long orderId);
	 	
}
