package com.example.mealkit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mealkit.domain.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long>{
	
	List<CartItem> findByCartId(Long CartId);

	//찜목록과 연동
	Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
	Optional<CartItem> findByIdAndCart_MemberId(Long id, Long memberId);
	
	//장바구니에서 주문 시 장바구니 비워짐
	List<CartItem> deleteByCart_Member_IdAndProduct_IdIn(Long memberId, List<Long> productIds);
	
	//추가
    // 선택삭제 (회원+아이디 리스트 기준)
    void deleteByIdInAndCart_MemberId(List<Long> ids, Long memberId);

    // 전체삭제 (회원기준)
    void deleteByCart_MemberId(Long memberId);
    
    
	//추가
	List<CartItem> findByIdInAndCart_MemberId(List<Long> ids, Long memberId);
	List<CartItem> findByCart_MemberId(Long memberId);


}
