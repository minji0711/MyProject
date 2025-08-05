package com.example.mealkit.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import lombok.*;

import com.example.mealkit.domain.Delivery;
import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.Mpoint;
import com.example.mealkit.repository.DeliveryRepository;
import com.example.mealkit.repository.MpointRepository;


import jakarta.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery")
public class DeliveryController {
	
	private final DeliveryRepository deliveryRepository;
	private final MpointRepository mpointRepository;
	
	
	// ** 관리자 배송처리 
	@Transactional
	@PostMapping("/{orderId}")
	public ResponseEntity<String> createDelivery(@PathVariable Long orderId, HttpSession session){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		if(!loginUser.isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한으로 로그인이 필요합니다.");	
		}
		
		Delivery delivery = deliveryRepository.findByOrdersId(orderId);
		
		if("COMPLETE".equals(delivery.getStatus())){
			return ResponseEntity.badRequest().body("이미 배송완료처리가 되었습니다.");			
		}
		
		delivery.setStatus("COMPLETE");
		deliveryRepository.save(delivery);	
		
		//구매포인트 적립(배송비 제외한 전체결제 금액의 5% 적립)
		int payment = delivery.getOrders().getPayment();
		int deliveryFee = delivery.getOrders().getDeliveryFee();
		int savingPoint = (int)(((payment-deliveryFee)*0.05)/10)*10;		//원 단위 절사  
		
		if (savingPoint > 0) {
			Mpoint mpoint = new Mpoint();
			mpoint.setMember(delivery.getMember());
			mpoint.setPoint(savingPoint);
			mpoint.setDescription("상품구매 포인트 적립");	
			mpoint.setOrders(delivery.getOrders());
			
			mpointRepository.save(mpoint);
		}
		
		return ResponseEntity.ok("배송완료 처리하였습니다.");					
	}	
	
}
