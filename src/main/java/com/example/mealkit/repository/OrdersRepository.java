package com.example.mealkit.repository;

import java.time.LocalDateTime;



import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mealkit.domain.Orders;
import com.example.mealkit.dto.RecentProductDto;

public interface OrdersRepository extends JpaRepository<Orders, Long>{
	
	// 회원 주문 조회
	List<Orders> findByMemberId(Long memberId);
			
	// 특정 기간 주문 조회
	List<Orders> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	//일정 금액이상 주문한 내역
	List<Orders> findByPaymentGreaterThanEqual(int amount);
	
	
	// 회원 주문 조회(주문 취소처리 제외된 주문내역)
	//List<Orders> findByMemberIdAndCancelReturnIsNull(Long memberId);
	
	List<Orders> findByMemberIdAndCancelReturnIsNullOrderByCreatedAtDesc(Long memberId);    
    List<Orders> findByCancelReturnIsNullOrderByCreatedAtDesc();

	
	//관리자 : 이번달 주문건수	
	@Query("SELECT COUNT(o) FROM Orders o " +
	       "WHERE o.createdAt BETWEEN :startOfMonth AND :endOfMonth " +
	       "AND o.cancelReturn IS NULL")
	long countThisMonthOrdersForAdmin(@Param("startOfMonth") LocalDateTime startOfMonth, 
									  @Param("endOfMonth") LocalDateTime endOfMonth);

	
	//관리자 : 이번달 총매출액
	@Query("SELECT COALESCE(SUM(o.amount), 0) FROM Orders o " +
	       "WHERE o.createdAt BETWEEN :startOfMonth AND :endOfMonth " +
	       "AND o.cancelReturn IS NULL")
	long findTotalSalesThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth,
	                             @Param("endOfMonth") LocalDateTime endOfMonth);
	
	
	//관리자 : 주문내역 조회(주문 취소처리 제외된 주문내역)
	List<Orders> findByCancelReturnIsNull();
	
	//추가
		// 회원 ID와 생성일 기준으로 주문 개수 조회
	    long countByMemberIdAndCreatedAtAfter(Long memberId, LocalDateTime createdAt);
		    
	 // 최근 주문한 상품만 간단히 조회 (상품명 3개 정도 가져오기 예시)
	    @Query("SELECT new com.example.mealkit.dto.RecentProductDto(p.id, p.name) " +
	            "FROM Orders o " +
	            "JOIN o.orderItems oi " +
	            "JOIN oi.product p " +
	            "WHERE o.member.id = :memberId " +
	            "GROUP BY p.id, p.name " +
	            "ORDER BY MAX(o.createdAt) DESC")
	    List<RecentProductDto> findRecentProducts(Long memberId, Pageable pageable);

}
