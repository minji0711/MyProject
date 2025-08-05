package com.example.mealkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mealkit.domain.OrderItem;


public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{

	// orders 의 상세주문상품 내역
	List<OrderItem> findByOrdersId(Long ordersId);
	
	// 특정 상품이 포함된 주문내역
	List<OrderItem> findByProductId(Long productId);
	
	
	// 주문상품의 작성 가능한 리뷰내역
	@Query("""
	        SELECT oi FROM OrderItem oi
	        WHERE oi.orders.member.id = :memberId
	          AND oi.orders.delivery.status = 'COMPLETE'
	          AND NOT EXISTS (
	              SELECT 1 FROM Review r
	              WHERE r.orderItem = oi
	          )
	    """)
	List<OrderItem> findReviewableItemsByMemberId(@Param("memberId") Long memberId);

}
