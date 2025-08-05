package com.example.mealkit.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.mealkit.domain.Coupon;
import com.example.mealkit.domain.Mcoupon;
import com.example.mealkit.domain.Member;
import com.example.mealkit.dto.McouponDto;
import com.example.mealkit.repository.McouponRepository;

import jakarta.servlet.http.HttpSession;
//import jakarta.transaction.Transactional;

import com.example.mealkit.repository.CouponRepository;

import lombok.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mcoupons")
public class McouponController {
	
	private final McouponRepository mcouponRepository;
	private final CouponRepository couponRepository;
	
	// **마이페이지 > 쿠폰 > 사용가능한 쿠폰(N), 사용완료 쿠폰(Y) 조회(회원)
	@GetMapping("/member/{type}")
	public ResponseEntity<?> listMcoupon (@PathVariable String type, HttpSession session){
		
		Member loginUser = (Member)session.getAttribute("loginMember");	
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		//추가
	    // 프론트에서 온 타입(unused/used)을 실제 DB의 값(N/Y)으로 매핑
	    String useYn;
	    switch (type) {
	        case "unused": useYn = "N"; break;
	        case "used": useYn = "Y"; break;
	        case "expired": useYn = "P"; break;
	        default: return ResponseEntity.badRequest().body("유효하지 않은 쿠폰 타입입니다.");
	    }
			
		
		List<Mcoupon> mcoupons = mcouponRepository.findByMemberIdAndUseYn(loginUser.getId(), useYn);		
		if(mcoupons.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
				
		// Mcoupon -> McouponDto로 매핑(변환)
		List<McouponDto> dtoList = mcoupons.stream()	//stream() : 컬렉션 데이터를 흐름(Stream) 형태로 변환하는 메서드 (컬렉션을 Stream으로 변환해서 연속된 연산 가능하게 함)
				.map(mc -> new McouponDto(				// → 각 Mcoupon 객체를 McouponDto 객체로 바꿉니다. → map()은 "변환" 함수입니다.
						mc.getId(),
						mc.getMember().getId(),
						mc.getCoupon().getTitle(), 
						mc.getCoupon().getRate(), 
						mc.getUseStart(),				//회원가입일에 따라 사용기간이 달라짐.(for person)
						mc.getUseEnd(), 
						mc.getUseYn(),
						mc.getUsedAt()
				))
				.toList();								// 변환된 McouponDto 객체들을 리스트로 모아 반환함.
		
		return ResponseEntity.ok(dtoList);
	}
	
	
	// **상품상세페이지 > 쿠폰노출 (관리자페이지에서 현재 기간내 max2개 쿠폰 출력)
	@GetMapping
	public ResponseEntity<List<Coupon>> getCoupon(){
		
		List<Coupon> coupons = couponRepository.findTop2ByTypeAndUseStartLessThanEqualAndUseEndGreaterThanEqualOrderByIdAsc("T", LocalDate.now(), LocalDate.now());		
		if(coupons.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.ok(coupons);		
	}
	
	
	// **상품상세페이지 > 쿠폰 다운로드 
	@PostMapping("/download/{couponId}")
	public ResponseEntity<Object> downCoupon(@PathVariable Long couponId, HttpSession session){
		
		Member loginUser = (Member)session.getAttribute("loginMember");			
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		// 쿠폰 존재 여부 확인
	    Coupon coupon = couponRepository.findById(couponId)
	            .orElseThrow(() -> new RuntimeException("해당 쿠폰이 존재하지 않습니다."));
		
		// 다운받은 쿠폰인지 체크
		Optional<Mcoupon> existcoupon = mcouponRepository.findByMemberIdAndCouponId(loginUser.getId(), couponId);
		
		// 이미 쿠폰을 다운로드 한 경우
		if(existcoupon.isPresent()) {
			return ResponseEntity.ok("이미 해당쿠폰을 다운로드하였습니다.");	
		} 
		
		// 쿠폰을 다운로드 하지 않은 경우.	 
		Mcoupon mcoupon = new Mcoupon();	
		mcoupon.setMember(loginUser);
		mcoupon.setCoupon(coupon);
		mcoupon.setUseYn("N");
		mcoupon.setUseStart(coupon.getUseStart());
		mcoupon.setUseEnd(coupon.getUseEnd());
		mcouponRepository.save(mcoupon);
		
		return ResponseEntity.ok("해당 쿠폰을 다운로드하였습니다.");	  					
	}
	
	
	// **주문페이지 > 사용가능 쿠폰 조회
	@GetMapping("/orders")
	public ResponseEntity<?> getMcoupon(HttpSession session){
		
		Member loginUser = (Member)session.getAttribute("loginMember");
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");		// 401 인증오류
		}
		
		List<Mcoupon> mcoupons = mcouponRepository.findByMemberIdAndUseYn(loginUser.getId(), "N");
		
		List<McouponDto> dtoList = mcoupons.stream()
				.map(mc -> new McouponDto(
						mc.getId(),
						mc.getMember().getId(),
						mc.getCoupon().getTitle(), 
						mc.getCoupon().getRate(), 
						mc.getUseStart(),				//회원가입일에 따라 사용기간이 달라짐.(for person)
						mc.getUseEnd(), 
						mc.getUseYn(),
						mc.getUsedAt()	
						))
				.toList();
		
		return ResponseEntity.ok(dtoList);		
	}
	
	
	// **사용가능한 쿠폰 갯수 조회
	@GetMapping("/mcouponCnt")
	public ResponseEntity<?> getMcouponCnt(HttpSession session) {
		Member loginUser = (Member)session.getAttribute("loginMember");
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");		// 401 인증오류
		}
		
		int cnt =  mcouponRepository.countByMemberIdAndUseYn(loginUser.getId(), "N");
		
		return ResponseEntity.ok(cnt);
	}	

	//추가
	@GetMapping("/downloadable")
	public ResponseEntity<List<Coupon>> getDownloadableCoupons() {
	    List<Coupon> coupons = couponRepository
	        .findTop2ByTypeAndUseStartLessThanEqualAndUseEndGreaterThanEqualOrderByIdAsc("T", LocalDate.now(), LocalDate.now());
	    return coupons.isEmpty() ? ResponseEntity.ok(Collections.emptyList()) : ResponseEntity.ok(coupons);
	}	
			
}
