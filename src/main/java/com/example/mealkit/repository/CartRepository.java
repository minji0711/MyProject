package com.example.mealkit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mealkit.domain.Cart;

public interface CartRepository extends JpaRepository<Cart, Long>{

	//회원당 1개의 장바구니를 갖고있으므로 list보단 optional
	Optional<Cart> findByMemberId(Long memberId);
}
