package com.example.mealkit.controller;

 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;



import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import lombok.*;

import com.example.mealkit.domain.Orders;
import com.example.mealkit.domain.Product;
import com.example.mealkit.dto.OrderAdminDto;
import com.example.mealkit.dto.OrderItemViewDto;
import com.example.mealkit.dto.OrderListDto;
import com.example.mealkit.dto.OrderRequestDto;
import com.example.mealkit.dto.OrderViewDto;
import com.example.mealkit.dto.RecentProductDto;
import com.example.mealkit.domain.Delivery;
import com.example.mealkit.domain.Mcoupon;
import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.Mpoint;
import com.example.mealkit.domain.OrderItem;
import com.example.mealkit.domain.Destination;
import com.example.mealkit.repository.OrdersRepository;
import com.example.mealkit.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;


import com.example.mealkit.repository.CartItemRepository;
import com.example.mealkit.repository.DeliveryRepository;
import com.example.mealkit.repository.DestinationRepository;
import com.example.mealkit.repository.McouponRepository;
import com.example.mealkit.repository.MpointRepository;
import com.example.mealkit.repository.OrderItemRepository;

import org.springframework.data.domain.PageRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrdersController {

	private final OrdersRepository ordersRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;
	private final McouponRepository mcouponRepository;
	private final MpointRepository mpointRepository;
	private final CartItemRepository cartItemRepository;
	private final DeliveryRepository deliveryRepository;
	private final DestinationRepository destinationRepository;
	
	
	// **상품 상세페이지 > 구매하기
	@PostMapping
	@Transactional
	public ResponseEntity<?> createOrder(@RequestBody OrderRequestDto dto, HttpSession session){
		
		Member loginUser = (Member)session.getAttribute("loginMember");		
		if (loginUser == null) {		
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");		// 401 인증오류
		}
		
		Orders order = new Orders();

		if (dto.getMcouponId() != null) {
			Mcoupon mcoupon = mcouponRepository.findByMemberIdAndId(loginUser.getId(), dto.getMcouponId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "쿠폰을 찾을 수 없습니다."));
			//.orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다"));
				
			mcoupon.setUseYn("Y");
			mcoupon.setUsedAt(LocalDateTime.now());
			mcouponRepository.save(mcoupon);
			
			order.setMcoupon(mcoupon);
			order.setDiscountCoupon(dto.getDiscountCoupon());
		}
		
		//포인트 처리
		int usedPoint = dto.getUsedPoint();
		if(usedPoint > 0) {
			Integer totMpoint = Optional.ofNullable(mpointRepository.findTotalPointByMemberId(loginUser.getId()))
					.orElse(0);
			
			if(totMpoint < dto.getUsedPoint()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용가능 포인트가 부족합니다.");
			}
			
			Mpoint mpoint = new Mpoint();
			mpoint.setMember(loginUser);
			mpoint.setPoint(-usedPoint);
			mpoint.setDescription("상품구매 차감");
			mpoint.setOrders(order);	
			mpointRepository.save(mpoint);

		}
		
		// 주문상품 처리(Orders, OrderItem 엔티티)
		Destination destination = destinationRepository.findById(dto.getDestinationId()).orElseThrow();
				
		order.setMember(loginUser);
		order.setDestination(destination);
		order.setDeliveryFee(dto.getDeliveryFee());
		order.setDeliveryRequest(dto.getDeliveryRequest());
		order.setPaymentMethod(dto.getPaymentMethod());
		order.setPayment(dto.getPayment());			// 배송비 포함가격
		order.setAmount(dto.getAmount());
		
		List<OrderItem> orderItems = dto.getOrderItemDtos().stream()
				.map(itemDto -> {
					Product product = productRepository.findById(itemDto.getProductId())
	                        .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));
					
					return new OrderItem(itemDto.getQuantity(), product.getPrice(), order, product);
				}).toList();
					
		order.setOrderItems(orderItems);
		ordersRepository.save(order);
		
		
		// 배송 처리 (READY,COMPLETE)
		Delivery delivery = new Delivery();
		delivery.setOrders(order);
		delivery.setMember(loginUser);
		delivery.setStatus("READY");
		deliveryRepository.save(delivery);
		
		// 장바구니에서 주문상품 제거
		List<Long> orderProductIds = dto.getOrderItemDtos().stream()
				.map(orderItemDto -> orderItemDto.getProductId())										// .map(orderItemDto::getProductId()) = .map(orderItemDto -> orderItemDto.getProductId())  
				.toList();
		
		if (!orderProductIds.isEmpty()) {
			cartItemRepository.deleteByCart_Member_IdAndProduct_IdIn(loginUser.getId(), orderProductIds);   
		}
		
		return ResponseEntity.ok("주문이 완료되었습니다.");		
	}
	
	
	// **마이페이지 > 30일이내 총 주문건수 조회
	@GetMapping("/ordercnt")
	public ResponseEntity<?> getOrderCnt(HttpSession session){
		Member loginUser = (Member)session.getAttribute("loginMember");
		
		if (loginUser == null) {		
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");		// 401 인증오류
		}
		
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		
		long totOrderCnt = ordersRepository.countByMemberIdAndCreatedAtAfter(loginUser.getId(), thirtyDaysAgo);
		
		return ResponseEntity.ok(totOrderCnt);			
	}
	
	
	// ** 마이페이지 > 주문내역 리스트
	@GetMapping("/mypage")
	public ResponseEntity<?> listOrder(HttpSession session){
		
		Member loginUser = (Member)session.getAttribute("loginMember");
	
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		// 주문 취소된 오더 제외한 주문내역
		List<Orders> orders = ordersRepository.findByMemberIdAndCancelReturnIsNullOrderByCreatedAtDesc(loginUser.getId());
		
		List<OrderListDto> listDtos = orders.stream()
				.map(order -> {
					List<OrderItem> items = order.getOrderItems();
					String firstProductName = items.get(0).getProduct().getName();
					String firstProductImage = items.get(0).getProduct().getImagePath();
					int itemCnt = items.size();
					String productSummary = itemCnt > 1 ? firstProductName + " 외 " + (itemCnt - 1) + "건" : firstProductName;
					String deliveryStatus = "COMPLETE".equals(order.getDelivery().getStatus()) ? "배송완료" : "배송전";
					
					return new OrderListDto(
							order.getId(), 
							order.getCreatedAt(), 
							productSummary, 
							order.getAmount(), 
							firstProductImage,
							order.getDelivery().getId(),
							order.getDestination().getReceiver(),	
							deliveryStatus,
							order.getMember().getId(),
			                order.getPoint()
					);
				})
				.toList();
		
		return ResponseEntity.ok(listDtos);
	}
	
	
	// ** 마이페이지 > 주문내역 > 주문상세 > 주문상품 리스트(1) 
	@GetMapping("/mypage/orderView1/{orderId}")
	public ResponseEntity<?> viewOrder(@PathVariable Long orderId, HttpSession session){
		
		Member loginUser = (Member)session.getAttribute("loginMember");
		
		if(loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("주문정보가 존재하지 않습니다."));
		
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
	
	
	// ** 마이페이지 > 주문내역 > 주문상세 > 결제 및 배송정보(2)
	@GetMapping("/mypage/orderView2/{orderId}")
	public ResponseEntity<OrderViewDto> viewOrder2(@PathVariable Long orderId){
		
		Orders orders = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("주문정보가 존재하지 않습니다."));
				
		String receiverAddr = "[" + orders.getDestination().getZipCode() + "]" + orders.getDestination().getAddress() + orders.getDestination().getAddressDetail();
		
		OrderViewDto dto = new OrderViewDto(
				orders.getAmount(),
				orders.getDiscountCoupon(), 
				orders.getPoint(), 
				orders.getDeliveryFee(), 
				orders.getPayment(),
				orders.getPaymentMethod(),
				orders.getDestination().getReceiver(),
				orders.getDestination().getPhone(),
				receiverAddr, 
				//orders.getDeliveryRequest()
				convertDeliveryRequest(orders.getDeliveryRequest())  // 변환함수 적용
				);
		
		return ResponseEntity.ok(dto);		
	}
	
	//함수 추가
	// private 메서드로 컨트롤러 안에 추가
	private String convertDeliveryRequest(String deliveryRequest) {
	    if (deliveryRequest == null || deliveryRequest.isEmpty()) {
	        return "정보없음";
	    }
	    switch (deliveryRequest) {
	        case "1": return "배송 전 연락 바랍니다.";
	        case "2": return "부재시, 전화 또는 연락 주세요.";
	        case "3": return "문앞에 두고 가주세요.";
	        default: return deliveryRequest;  // 직접 입력한 메시지 그대로 출력
	    }
	}

	
	// **관리자: 이번달 총 주문 건수
	@GetMapping("/admin/totOrders")
	public ResponseEntity<?> getTotOrder(){
		LocalDate now = LocalDate.now();
		LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
		LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);

		long totalOrders = ordersRepository.countThisMonthOrdersForAdmin(startOfMonth, endOfMonth);
		
		return ResponseEntity.ok(totalOrders);
	}
	
	
	// **관리자: 이번달 총 매출액
	@GetMapping("/admin/totAmount")
	public ResponseEntity<?> getTotAmount(){
		LocalDate now = LocalDate.now();
		LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
		LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);

		Long totalSales = ordersRepository.findTotalSalesThisMonth(startOfMonth, endOfMonth);
		
		return ResponseEntity.ok(totalSales);
		
	}
	
	
	// **관리자: 주문내역조회
	@GetMapping("/admin/orders")
	public ResponseEntity<?> listAdminOrder(HttpSession session){
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		if (!loginUser.isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한으로 로그인 하세요."); 
		}
		
		//List<Orders> orders = ordersRepository.findByCancelReturnIsNull();
		// 최신순 정렬된 주문내역 조회
	    List<Orders> orders = ordersRepository.findByCancelReturnIsNullOrderByCreatedAtDesc();
		
		if (orders.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		List<OrderAdminDto> adminDto = orders.stream()
				.map(order -> {
					List<OrderItem> items = order.getOrderItems();
					String firstProductName = items.get(0).getProduct().getName();
					//String firstProductImage = items.get(0).getProduct().getImagePath();
					int itemCnt = items.size();
					String productSummary = itemCnt > 1 ? firstProductName + " 외 " + (itemCnt - 1) + "건" : firstProductName;
					
					
					String deliveryStatus;					
					switch (order.getDelivery().getStatus().toUpperCase()) {
					case "READY":
						deliveryStatus = "배송전";
						break;
						
					case "COMPLETE":
						deliveryStatus ="배송완료";
						break;
						
					default:
						deliveryStatus ="기타";
						break;
					}
												
					return new OrderAdminDto(
							order.getId(),
							order.getDelivery().getId(),
							order.getCreatedAt(),
							order.getMember().getName(),
							productSummary,
							order.getAmount(),
							deliveryStatus		
							);
				})
				.toList();
		
		return ResponseEntity.ok(adminDto);		
	}
	
	//추가
	@PatchMapping("/admin/{orderId}/delivery")
	public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long orderId, HttpSession session) {
	    Member loginUser = (Member) session.getAttribute("loginMember");

	    if (loginUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
	    }

	    if (!loginUser.isAdmin()) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한으로 로그인 하세요.");
	    }

	    // 주문 찾기
	    Orders order = ordersRepository.findById(orderId)
	            .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다."));

	    // 배송 찾기 (Orders → Delivery는 1:1 관계)
	    Delivery delivery = order.getDelivery();
	    if (delivery == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("배송 정보가 존재하지 않습니다.");
	    }

	    // 배송상태 업데이트 (배송완료)
	    delivery.setStatus("COMPLETE");
	    deliveryRepository.save(delivery);

	    return ResponseEntity.ok("배송 상태가 '배송완료'로 변경되었습니다.");
	}
	
	//추가
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentOrders(HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginMember");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        

     // 컨트롤러 메서드 내부
     PageRequest pageRequest = PageRequest.of(0, 3);
     List<RecentProductDto> recentProducts = ordersRepository.findRecentProducts(loginUser.getId(), pageRequest);

        return ResponseEntity.ok(recentProducts);
    }
    
}



