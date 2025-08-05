package com.example.mealkit.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mealkit.domain.Coupon;
import com.example.mealkit.domain.Mcoupon;
import com.example.mealkit.domain.Member;
import com.example.mealkit.repository.CouponRepository;
import com.example.mealkit.repository.McouponRepository;

import jakarta.transaction.Transactional;
import lombok.*;

@Service
@RequiredArgsConstructor
public class McouponService {
	
	private final McouponRepository mcouponRepository;
    private final CouponRepository couponRepository;
    
    
    // ##로그인 후 > 사용가능쿠폰 기간만료 처리(useYn=P) 
    // 컨트롤러에서 호출시: private final McouponService mcouponService; / mcouponService.checkExpireMcoupons(loginMember);
 	public void checkExpireMcoupons(Long memberId) {
 		List<Mcoupon> mcoupons = mcouponRepository.findByMemberId(memberId);
 		
 		for (Mcoupon mcoupon : mcoupons) {
 	        if (mcoupon.isExpired(LocalDate.now())) {
 	            mcoupon.setUseYn("P");
 	        }
 	    }
 			 
 	}
 	
 	
 	// ##회원가입 > 가입완료 후 자동쿠폰 발행(coupon type = M)
 	// 컨트롤러에서 호출시: private final McouponService mcouponService; / mcouponService.signupMcoupon(member);
 	public void signupMcoupon(Member member) {
 		Coupon coupon = couponRepository.findTop1ByType("M");
 		
 		if(coupon != null) {			
 			boolean exists = mcouponRepository.existsByMemberAndCoupon_Type(member, "M");
 			if (!exists) {
 			
 				//관리자가 발행한 회원가입 축하쿠폰이 존재하는 경우			
 				Mcoupon mcoupon = new Mcoupon();
 				mcoupon.setMember(member);
 				mcoupon.setCoupon(coupon);
 				mcoupon.setUseYn("N");
 				mcoupon.setUseStart(LocalDate.now());
 				mcoupon.setUseEnd(LocalDate.now().plusMonths(1));
 				mcouponRepository.save(mcoupon);
 			}
 		}
 	}
 	

}
