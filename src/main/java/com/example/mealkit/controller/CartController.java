package com.example.mealkit.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.mealkit.domain.Cart;
import com.example.mealkit.domain.CartItem;
import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.Product;
import com.example.mealkit.dto.CartItemDeleteRequestDto;
import com.example.mealkit.dto.CartItemRequestDto;
import com.example.mealkit.dto.CartItemResponseDto;
import com.example.mealkit.repository.CartItemRepository;
import com.example.mealkit.repository.CartRepository;
import com.example.mealkit.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


//장바구니에서 선택해서 주문하기는 리액트로 구현
//주문 후 장바구니 비워지는거는 orders로 하기
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // 장바구니에 상품 추가
    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@RequestBody CartItemRequestDto dto, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).body("로그인 필요");

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setMember(member);
            return cartRepository.save(newCart);
        });
        
        if (dto.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("수량은 1 이상이어야 합니다.");
        }

        
        if (cart.getCartItems().size() >= 30) {
            return ResponseEntity.badRequest().body("장바구니에는 최대 30개의 상품만 담을 수 있습니다.");
        }
        
        // 기존 상품 존재 여부 확인
        Optional<CartItem> existing = cartItemRepository
        		.findByCartIdAndProductId(cart.getId(), product.getId());


        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + dto.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(dto.getQuantity());
            cartItemRepository.save(newItem);
        }

        return ResponseEntity.ok("장바구니 추가 완료");
    }

    // 장바구니 목록
    @GetMapping("/items")
    public ResponseEntity<?> getCartItems(HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).body("로그인 필요");

        Cart cart = cartRepository.findByMemberId(member.getId()).orElse(null);
        if (cart == null || cart.getCartItems().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<CartItemResponseDto> response = new ArrayList<>();
        int totalPrice = 0;

        for (CartItem item : cart.getCartItems()) {
            Product p = item.getProduct();

            CartItemResponseDto dto = new CartItemResponseDto();
            dto.setCartItemId(item.getId());
            dto.setProductId(p.getId());
            dto.setProductName(p.getName());
            dto.setImagePath(p.getImagePath());
            dto.setQuantity(item.getQuantity());
            dto.setPrice(p.getPrice());
            dto.setTotalPrice(p.getPrice() * item.getQuantity());

            totalPrice += dto.getTotalPrice();
            response.add(dto);
        }

        int deliveryFee = totalPrice >= 30000 ? 0 : 3000;
        return ResponseEntity.ok(Map.of(
                "items", response,
                "totalPrice", totalPrice,
                "deliveryFee", deliveryFee,
                "finalTotal", totalPrice + deliveryFee
        ));
    }

    // 수량 수정
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long itemId, @RequestBody CartItemRequestDto dto, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).body("로그인 필요");
        
        if (dto.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("수량은 1 이상이어야 합니다.");
        }


        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목 없음"));

        item.setQuantity(dto.getQuantity());
        cartItemRepository.save(item);

        return ResponseEntity.ok("수량 수정 완료");
    }

    @DeleteMapping("/items")
    @Transactional
    public ResponseEntity<?> deleteCartItems(@RequestBody CartItemDeleteRequestDto dto, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        if (dto.getCartItemIds() == null || dto.getCartItemIds().isEmpty()) {
            // 전체삭제
            cartItemRepository.deleteByCart_MemberId(member.getId());
            return ResponseEntity.ok("삭제 완료");  // 전체삭제/선택삭제 메시지 통일
        }

        // 선택삭제
        List<CartItem> itemsToDelete = cartItemRepository.findByIdInAndCart_MemberId(dto.getCartItemIds(), member.getId());
        if (itemsToDelete.isEmpty()) {
            return ResponseEntity.badRequest().body("삭제할 수 있는 항목이 없습니다.");
        }

        cartItemRepository.deleteAll(itemsToDelete);
        return ResponseEntity.ok("삭제 완료");
    }    

    
}

