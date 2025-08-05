package com.example.mealkit.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;

import lombok.*;

import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.Mpoint;
import com.example.mealkit.domain.OrderItem;
import com.example.mealkit.domain.Orders;
import com.example.mealkit.domain.Product;
import com.example.mealkit.domain.Review;
import com.example.mealkit.dto.ReviewCountDto;
import com.example.mealkit.dto.ReviewListDto;
import com.example.mealkit.dto.ReviewMypageDto;
import com.example.mealkit.dto.ReviewResponseDto;
import com.example.mealkit.repository.MpointRepository;
import com.example.mealkit.repository.OrderItemRepository;
import com.example.mealkit.repository.ReviewRepository;
import com.example.mealkit.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
	
	@Value("${upload.path}")
	private String uploadPath;
	
	private final ReviewRepository reviewRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;
	private final MpointRepository mpointRepository;
	
	// **상품별 리뷰내역
	@GetMapping("/{productId}")
	public ResponseEntity<?> listReview(@PathVariable Long productId, HttpSession session){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		List<Review> review = reviewRepository.findByProductId(productId);
		
		if(review.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("등록된 리뷰가 없습니다.");
		}
		
		return ResponseEntity.ok(review);		
		
	}
	
	
	// **모든 리뷰내역 (관리자)
	@GetMapping("/admin/review")
	public ResponseEntity<?> listAllReview(HttpSession session){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		if(!loginUser.isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한으로 로그인이 필요합니다.");	
		}
		
		//List<Review> list = reviewRepository.findAll();

	    // ✅ 작성일 기준 내림차순 정렬된 리뷰 가져오기
	    List<Review> list = reviewRepository.findAllByOrderByCreatedAtDesc();

		
		if(list.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("등록된 리뷰가 없습니다.");
		}
		
		List<ReviewListDto> dto = list.stream()
	            .map(item -> new ReviewListDto(
	                    item.getId(),
	                    item.getMember().getName(),
	                    item.getMember().getEmail(),
	                    item.getOrderItem().getOrders().getCreatedAt(),   // 주문일 경로 정확히 이렇게
	                    item.getProduct().getId(),
	                    item.getProduct().getName(),
	                    item.getScore(),
	                    item.getContent(),
	                    item.getCreatedAt(),
	                    item.getImagePath(),
	                    item.getImagePath2()
	            ))
	            .toList();
		
		return ResponseEntity.ok(dto);
	}
	
	
	// **마이페이지 > 작성 가능한 리뷰내역
	@GetMapping("/mypage/reviewable")
	public ResponseEntity<?> listMypageReview(HttpSession session){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		List<OrderItem> orderItem = orderItemRepository.findReviewableItemsByMemberId(loginUser.getId());
		
		if(orderItem.isEmpty()) {
			return ResponseEntity.badRequest().body("작성 가능한 리뷰가 없습니다.");
		}
		
		List<ReviewMypageDto> reviewDto = orderItem.stream()
				.map(item -> new ReviewMypageDto(
						item.getOrders().getId(),
						item.getProduct().getId(),
						item.getOrders().getCreatedAt(),
						item.getProduct().getName(),
						item.getProduct().getImagePath(),
						item.getId()
						))
				.toList();	
		
		return ResponseEntity.ok(reviewDto);	
	}
	
		
	// **마이페이지 > 리뷰 > 작성 완료한 리뷰내역
	@GetMapping("/mypage/reviewed")
	public ResponseEntity<?> listWrittenReview(HttpSession session){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		List<Review> review = reviewRepository.findByMemberId(loginUser.getId());
		
		if(review.isEmpty()) {
			return ResponseEntity.badRequest().body("작성한 리뷰가 없습니다.");
		}
		
		List<ReviewMypageDto> reviewDto = review.stream()
				.map(item -> new ReviewMypageDto(
						item.getOrderItem().getOrders().getId(),
						item.getProduct().getId(),
						item.getOrderItem().getOrders().getCreatedAt(),
						item.getProduct().getName(),
						item.getProduct().getImagePath(),
						item.getCreatedAt(),
						item.getOrderItem().getId(),
						item.getScore(),
						item.getContent(),
						item.getImagePath(),
						item.getImagePath2(),
						item.getId()
						))
				.toList();
		
		return ResponseEntity.ok(reviewDto);		
	}
		
	// **리뷰 작성하기
	@Transactional
	@PostMapping
	public ResponseEntity<?> createReview(@ModelAttribute ReviewResponseDto dto, HttpSession session){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		// orderItem, orders, product 객체 조회
	    OrderItem orderItem = orderItemRepository.findById(dto.getOrderItemId()).orElseThrow();
	    Orders orders = orderItem.getOrders();
	    Product product = productRepository.findById(dto.getProductId()).orElseThrow();
		
		//업로드파일 폴더가 없는 경우.
		File uploadDir = new File(uploadPath).getAbsoluteFile();	
		if(!uploadDir.exists()) uploadDir.mkdirs();
		
		String fileName1 = null;
	    String fileName2 = null;
		
	    try {
	    	
			//중복파일명 처리 및 저장 (UUID: 자바 유틸리티에서 사용하는 랜덤함수 )
		    if (dto.getImagePath1() != null && !dto.getImagePath1().isEmpty()) {
		    	fileName1  = UUID.randomUUID() +"_"+ dto.getImagePath1().getOriginalFilename();
		    	File dest1 = new File(uploadDir, fileName1);
		    	dto.getImagePath1().transferTo(dest1);
		    }
	
		    if (dto.getImagePath2() != null && !dto.getImagePath2().isEmpty()) {
				fileName2 = UUID.randomUUID() +"_"+ dto.getImagePath2().getOriginalFilename();
				File dest2 = new File(uploadDir, fileName2);
				dto.getImagePath2().transferTo(dest2);
		    }	
		    
	    } catch (IOException | IllegalStateException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패"); 	
	    }
	    
		Review review = new Review();
		review.setScore(dto.getScore());
		review.setContent(dto.getContent());
		review.setOrderItem(orderItem);
		review.setMember(loginUser);
		review.setProduct(product);
		if (fileName1 != null) {
			review.setImagePath("/images/"+fileName1);
		}
		if (fileName2 != null) {
			review.setImagePath2("/images/"+fileName2);
		}
		reviewRepository.save(review);
		
		//리뷰포인트 적립 (일반:300P, 사진:500P)
		Mpoint mpoint = new Mpoint();
		mpoint.setMember(loginUser);
		mpoint.setOrders(orders);
		mpoint.setReview(review);
		
		//일반 리뷰포인트 적립
		if((dto.getImagePath1() == null && dto.getImagePath2() == null) || (dto.getImagePath1().isEmpty() && dto.getImagePath2().isEmpty())){
			mpoint.setPoint(300);
			mpoint.setDescription("일반 리뷰포인트 지급")	;
			
		//사진 리뷰포인트 적립
		} else {
			mpoint.setPoint(500);
			mpoint.setDescription("사진 리뷰포인트 지급")	;	
		}	
		mpointRepository.save(mpoint);
		
		return ResponseEntity.ok("리뷰 작성을 완료하였습니다.\n\\n리뷰작성 포인트도 지급되었습니다.");	
	}
	
	
	// **리뷰 수정하기
	@Transactional
	@PutMapping("{reviewId}")
	public ResponseEntity<?> updateReview(@PathVariable Long reviewId, @ModelAttribute ReviewResponseDto dto, HttpSession session ){
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		Optional<Review> optional = reviewRepository.findById(reviewId);
		
		Review review = optional.orElseThrow(() -> new RuntimeException("리뷰가 존재하지 않습니다."));
		
		review.setScore(dto.getScore());
		review.setContent(dto.getContent());
		
		//이미지가 변경된 경우
		String fileName1 = null;
	    String fileName2 = null;
		try {
			//기존 이미지 삭제
			if(review.getImagePath() != null && !review.getImagePath().isEmpty()) {
				File oldFile = new File(new File(uploadPath), review.getImagePath().replace("/images", ""));			
				if(oldFile.exists()) oldFile.delete();				
			}
			
			if(review.getImagePath2() != null && !review.getImagePath2().isEmpty()) {
				File oldFile = new File(new File(uploadPath), review.getImagePath2().replace("/images", ""));			
				if(oldFile.exists()) oldFile.delete();				
			}
			
			//중복파일명 처리 및 저장 (UUID: 자바 유틸리티에서 사용하는 랜덤함수 )
		    if (dto.getImagePath1() != null && !dto.getImagePath1().isEmpty()) {
		    	fileName1  = UUID.randomUUID() +"_"+ dto.getImagePath1().getOriginalFilename();
		    	File dest1 =new File(new File(uploadPath).getAbsoluteFile(), fileName1);
		    	dto.getImagePath1().transferTo(dest1);
		    	review.setImagePath("/images/"+fileName1);	
		    }
	
		    if (dto.getImagePath2() != null && !dto.getImagePath2().isEmpty()) {
				fileName2 = UUID.randomUUID() +"_"+ dto.getImagePath2().getOriginalFilename();
				File dest2 = new File(new File(uploadPath).getAbsoluteFile(), fileName2);
				dto.getImagePath2().transferTo(dest2);
				review.setImagePath2("/images/"+fileName2);
		    }	
		    
	    } catch (IOException | IllegalStateException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패"); 	
	    }
		
		reviewRepository.save(review);
	
		// 포인트 수정.
		return ResponseEntity.ok("리뷰가 수정되었습니다.");	
	}
	
	
	// **리뷰 삭제하기
	
	@Transactional
	@DeleteMapping("{reviewId}")
	public ResponseEntity<String> deleteReview(@PathVariable Long reviewId, HttpSession session){
		
		
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		Optional<Review> optional = reviewRepository.findById(reviewId);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();	
		}
						
		Review review = optional.get();
		    
		
		if(review.getImagePath() != null && !review.getImagePath().isEmpty()) {
			File imageFile = new File(new File(uploadPath), review.getImagePath().replace("/images", ""));			
			if(imageFile.exists()) imageFile.delete();					
		}
		if(review.getImagePath2() != null && !review.getImagePath2().isEmpty()) {
			File imageFile = new File(new File(uploadPath), review.getImagePath2().replace("/images", ""));			
			if(imageFile.exists()) imageFile.delete();				
		}
		

		reviewRepository.delete(review);
		
		/*
		// 리뷰 포인트 차감
		Mpoint mpoint = new Mpoint();
		mpoint.setMember(loginUser);
		mpoint.setOrders(review.getOrderItem().getOrders());
		mpoint.setReview(review);
		
		//일반 리뷰포인트 적립
		if((review.getImagePath() == null && review.getImagePath2() == null) || (review.getImagePath().isEmpty() && review.getImagePath2().isEmpty())){
			mpoint.setPoint(-300);
			mpoint.setDescription("일반리뷰 삭제에 따른 리뷰포인트 차감")	;
			
		//사진 리뷰포인트 적립
		} else {
			mpoint.setPoint(-500);
			mpoint.setDescription("사진리뷰 삭제에 따른 리뷰포인트 차감")	;	
		}	
		mpointRepository.save(mpoint);
		*/
		return ResponseEntity.ok("리뷰를 삭제하였습니다.\n\n리뷰작성시 지급된 포인트도 차감되었습니다.");		
	}
	
	
	// **상품별 리뷰별점_리뷰갯수 조회
	@GetMapping("/cnt/{productId}")
	public ResponseEntity<?> getReviewCnt(@PathVariable Long productId){
		
		List<Review> review = reviewRepository.findByProductId(productId);
		
		if (review.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		//리뷰 갯수
		int reviewCnt = review.size();
		
		//리뷰 평균점수
		Double reviewAvg = reviewRepository.findAverageScoreByProductId(productId);
		 
		 if (reviewAvg == null) {
			 reviewAvg = 0.0;
		 }
		 
		 ReviewCountDto dto = new ReviewCountDto(reviewCnt, reviewAvg);
		 
		 return ResponseEntity.ok(dto);
		 	
	}
		

}
