package com.example.mealkit.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mealkit.domain.Cart;
import com.example.mealkit.domain.CartItem;
import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.Product;
import com.example.mealkit.domain.Wish;
import com.example.mealkit.dto.WishResponseDto;
import com.example.mealkit.repository.CartItemRepository;
import com.example.mealkit.repository.CartRepository;
import com.example.mealkit.repository.ProductRepository;
import com.example.mealkit.repository.WishRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wish")
@RequiredArgsConstructor
public class WishController {

	private final WishRepository wishRepository;
	private final ProductRepository productRepository;
	private final CartRepository cartRepository;	
	private final CartItemRepository cartItemRepository;
	
	@GetMapping
	public ResponseEntity<?> getWishes(HttpSession session) {
		Member member = (Member) session.getAttribute("loginMember");
		if (member == null) return ResponseEntity.status(401).body("로그인 필요");
	
		List<Wish> wishes = wishRepository.findByMemberId(member.getId());
		
		List<WishResponseDto> response = wishes.stream().map(w -> {
			Product p = w.getProduct();
			WishResponseDto dto = new WishResponseDto();
			dto.setWishId(w.getId());
			dto.setProductId(p.getId());
			dto.setProductName(p.getName());
			dto.setProductImage(p.getImagePath());
			dto.setPrice(p.getPrice());
			return dto;
		}).toList();
		return ResponseEntity.ok(response);
	}
	@PostMapping("/{productId}")
	public ResponseEntity<?> addWish(@PathVariable Long productId, HttpSession session) {
		Member member = (Member) session.getAttribute("loginMember");
		if (member == null) return ResponseEntity.status(401).body("로그인필요");
		
		if (wishRepository.existsByMemberIdAndProductId(member.getId(), productId)) {
			return ResponseEntity.badRequest().body(Map.of("error","이미 찜한 상품입니다."));
		}
		
		//찜 목록 최대 30개, 30개이상일 시 오래된 id부터 자동삭제
		List<Wish> currentWishes = wishRepository.findByMemberId(member.getId());
		if(currentWishes.size() >= 30) {
			wishRepository.findTopByMemberIdOrderByIdAsc(member.getId())
			.ifPresent(wishRepository::delete);	//wish -> wishRepository.delete(wish)
			//메서드 참조 문법
		}
		
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("상품 없음"));
		
		Wish wish = new Wish();
		wish.setMember(member);
		wish.setProduct(product);
		wishRepository.save(wish);
		
		return ResponseEntity.ok("찜 추가완료");
	}
	
	@DeleteMapping("/{wishId}")
	public ResponseEntity<?> deleteWish(@PathVariable Long wishId, HttpSession session) {
		Member member = (Member) session.getAttribute("loginMember");
		if (member == null) return ResponseEntity.status(401).body("로그인필요");
		 
		Wish wish = wishRepository.findById(wishId)
				.orElseThrow(() -> new IllegalArgumentException("짐 내역 없음"));
		if(!wish.getMember().getId().equals(member.getId())) {
			return ResponseEntity.status(403).body("본인만 삭제 가능");
		}
		wishRepository.delete(wish);
		return ResponseEntity.ok("삭제 완료");
	}
	
	@PostMapping("/deleteSelected")
	public ResponseEntity<?> deleteSelectedWishes(@RequestBody List<Long> wishIds, HttpSession session) {
		Member member = (Member) session.getAttribute("loginMember");
		if (member == null) return ResponseEntity.status(401).body("로그인 필요");
		
		List<Wish> toDelete = wishRepository.findAllById(wishIds).stream()
				.filter(w -> w.getMember().getId().equals(member.getId()))
				.toList();
		wishRepository.deleteAll(toDelete);
		return ResponseEntity.ok("선택 삭제 완료");
	}
	
	@PostMapping("/moveToCart")
	public ResponseEntity<?> meveToCart(@RequestBody List<Long> wishIds, HttpSession session)
		throws IOException {
		Member member = (Member) session.getAttribute("loginMember");
		if (member == null) return ResponseEntity.status(401).body("로그인 필요");		
		
		Cart cart = cartRepository.findByMemberId(member.getId())
				.orElseGet(() -> {
					Cart newCart = new Cart();
					newCart.setMember(member);
					return cartRepository.save(newCart);
				});
		for (Long wishId : wishIds) {
			Wish wish = wishRepository.findById(wishId)
					.orElseThrow(() -> new IllegalArgumentException("찜 항목 없음: " + wishId));
			Product product = wish.getProduct();
			
			CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
					.orElse(null);
			
			if (cartItem != null) {
				cartItem.setQuantity(cartItem.getQuantity() + 1);
				cartItemRepository.save(cartItem);
			} else {
				CartItem newCartItem = new CartItem();
				newCartItem.setCart(cart);
				newCartItem.setProduct(product);
				newCartItem.setQuantity(1);
				cartItemRepository.save(newCartItem);
			}
		}
		return ResponseEntity.ok("장바구니에 담았습니다.");
	}
}
 