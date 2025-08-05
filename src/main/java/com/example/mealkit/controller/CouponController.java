package com.example.mealkit.controller;

import com.example.mealkit.domain.*;
import com.example.mealkit.dto.CouponListDto;
import com.example.mealkit.repository.*;


import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.*;
import java.util.*;

@RestController					//@RestController: @Controller + @ResponseBody → 뷰를 반환하지 않고, HTTP 응답으로 JSON 또는 문자열 등의 데이터를 바로 반환하는 컨트롤러
@RequestMapping("/api/coupons")
@RequiredArgsConstructor		//final 필드나 @NonNull 필드만을 파라미터로 받는 생성자를 자동 생성합니다.
public class CouponController {
	
	private final CouponRepository couponRepository;
	
	// **쿠폰 생성(관리자)
	@PostMapping("/create")  
	public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
				
		Coupon savedCoupon = couponRepository.save(coupon);	
		return ResponseEntity.ok(savedCoupon);
	}
	// ResponseEntity는 Spring Web에서 클라이언트에게 HTTP 상태 코드, 응답 본문, 헤더 등을 함께 명확하게 반환할 수 있게 도와주는 클래스입니다.
	// 컨트롤러에서 REST API 응답을 커스터마이징할 때 많이 사용됩니다.
	
	
	// **쿠폰 조회(관리자)
	@GetMapping("/list")
	public ResponseEntity<?> listCoupon(HttpSession session){
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		if (!loginUser.isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한으로 로그인 하세요."); 
		}
		
		List<Coupon> coupon = couponRepository.findAll();
		
		if(coupon.isEmpty()) {
			return ResponseEntity.notFound().build();			
		}
		
		List<CouponListDto> listDto = coupon.stream()
				.map(item -> {
					//쿠폰 타입
					String type = "";
					switch (item.getType()) {
					case "M":
						type = "회원가입쿠폰";
						break;	
					case "T":
						type = "기간쿠폰";
						break;
					default:
						break;					
					}
					
					//쿠폰 상태
					String couponStatus = "";
					CouponStatus status = item.getStatus();
					switch (status) {
			        case NOTYET:
			        	couponStatus = "사용전";
			        	break;
			        case ACTIVE:
			        	couponStatus = "사용중";
			        	break;
			        case EXPIRED:
			        	couponStatus = "기간만료";
			        	break;
					default:
			        	break;
					}
					
					//쿠폰 사용기간
					String period = "";
					if ("T".equals(item.getType())) {
						period = item.getUseStart() +" ~ "+ item.getUseEnd();
					} else {
						period = "";
					}
					
				return new CouponListDto(
						item.getId(),
						type,
						item.getTitle(),
						item.getRate(),
						period,
						couponStatus						
						);	
				})
				.toList();
		
		return ResponseEntity.ok(listDto);	
	}	
	
	// **쿠폰 삭제(관리자)
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteCoupon(@PathVariable Long id) {		
		return couponRepository.findById(id)
				.map(delCoupon -> {
					couponRepository.delete(delCoupon);
					return ResponseEntity.ok("쿠폰을 삭제하였습니다."); 								
				})
				.orElse(ResponseEntity.notFound().build());		// 404 
	}	

}
