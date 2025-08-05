package com.example.mealkit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.mealkit.domain.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long>{
	
	// 특정회원의 배송정보 조회
	List<Delivery> findByMemberId(Long memberId);
	
	// OrderId로 배송정보 조회
	Delivery findByOrdersId(Long orderId);
	
}
