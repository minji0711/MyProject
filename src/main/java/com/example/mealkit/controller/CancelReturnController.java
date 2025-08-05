package com.example.mealkit.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import lombok.*;

import com.example.mealkit.domain.CancelReturn;
import com.example.mealkit.domain.Mcoupon;
import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.Mpoint;
import com.example.mealkit.domain.OrderItem;
import com.example.mealkit.domain.Orders;
import com.example.mealkit.dto.CancelRequestDto;
import com.example.mealkit.dto.OrderItemViewDto;
import com.example.mealkit.dto.OrderListDto;
import com.example.mealkit.repository.CancelReturnRepository;
import com.example.mealkit.repository.McouponRepository;
import com.example.mealkit.repository.MpointRepository;
import com.example.mealkit.repository.OrderItemRepository;
import com.example.mealkit.repository.OrdersRepository;
import com.example.mealkit.service.CancelReturnService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cancel")
public class CancelReturnController {
	
	private final CancelReturnRepository cancelReturnRepository;
	private final OrdersRepository ordersRepository;
	private final OrderItemRepository orderItemRepository;
	private final MpointRepository mpointRepository;
	private final McouponRepository mcouponRepository;
	private final CancelReturnService cancelReturnService;
	private final MpointController mpointController;
	
	// **마이페이지 > 주문내역 > 구매취소(배송완료 전 구매 취소 가능)
	@Transactional
	@PostMapping("/{orderId}")
	public ResponseEntity<String> createCancel(@PathVariable Long orderId, HttpSession session){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		Orders order = ordersRepository.findById(orderId)
				.orElseThrow(()-> new RuntimeException("주문정보가 존재하지 않습니다."));
		
		// 본인주문 여부 체크
		if(!loginUser.getId().equals(order.getMember().getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 주문 건만 취소가능합니다.");
		}
		
		// 배송완료 여부 체크
		if("COMPLETE".equals(order.getDelivery().getStatus())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("배송 완료된 주문은 취소가 불가능합니다.");
		}

		// 주문취소 여부 체크
		boolean existCancel = cancelReturnRepository.existsByOrders(order);
		
		if(existCancel) {
			return ResponseEntity.badRequest().body("이미 취소처리된 주문입니다.");
		}
		
		// 포인트 반환
		if(order.getPoint() > 0) {
			Mpoint mpoint = new Mpoint();
			mpoint.setMember(loginUser);
			mpoint.setPoint(order.getPoint());
			mpoint.setDescription("구매취소 포인트 반환");
			mpoint.setOrders(order);		
			mpointRepository.save(mpoint);
		}
		
		// 쿠폰 반환
		if (order.getMcoupon() != null) {
			Mcoupon mcoupon = order.getMcoupon();
			mcoupon.setUseYn("N");			
		}
		
		// 취문취소 처리			
		CancelReturn cancel = new CancelReturn();
		cancel.setOrders(order);
		cancel.setMember(loginUser);		
		cancelReturnRepository.save(cancel);
		
		return ResponseEntity.ok("주문 취소를 완료하였습니다.");	
	}
	
	// **마이페이지 > 주문취소 내역
	@GetMapping
	public ResponseEntity<?> listCancel(HttpSession session){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		List<CancelReturn> cancel = cancelReturnRepository.findByMemberIdOrderByCreatedAtDesc(loginUser.getId());
		
		if (cancel.isEmpty()) {
		    return ResponseEntity.badRequest().body("주문 취소내역이 없습니다.");
		}
				
		List<OrderListDto> listDtos = cancel.stream()
				.map(order -> {
					List<OrderItem> items = order.getOrders().getOrderItems();
					String firstProductName = items.get(0).getProduct().getName();
					String firstProductImage = items.get(0).getProduct().getImagePath();
					int itemCnt = items.size();					
					String productSummary = itemCnt > 1 ? firstProductName + " 외 " + (itemCnt - 1) + "건" : firstProductName;
					
					return new OrderListDto(
							order.getOrders().getId(), 
							order.getCreatedAt(), 
							productSummary, 
							order.getOrders().getAmount(), 
							firstProductImage);		
				})
				.toList();
		
		return ResponseEntity.ok(listDtos);	
	}
	
	
	// **마이페이지 > 주문취소 > 주문취소 상세내역(구매 물품목록)
	@GetMapping("/{orderId}")
	public ResponseEntity<?> getViewCancel(@PathVariable Long orderId, HttpSession session){
		
		Member loginUser = (Member)session.getAttribute("loginMember");
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		CancelReturn cancel = cancelReturnRepository.findByOrders_Id(orderId);
		if(cancel == null) {
			return ResponseEntity.badRequest().body("주문 취소내역이 없습니다.");
		}
		
		List<OrderItem> items = orderItemRepository.findByOrdersId(orderId);
		
		List<OrderItemViewDto> itemDtos = items.stream()
				.map(item -> new OrderItemViewDto(
						item.getProduct().getId(),
						item.getProduct().getName(),
						item.getPrice(),
						item.getProduct().getImagePath(),
						item.getQuantity()		
						))
				.toList();	
		
		return ResponseEntity.ok(itemDtos);	

	}

}
