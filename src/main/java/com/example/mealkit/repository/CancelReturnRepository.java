package com.example.mealkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.mealkit.domain.CancelReturn;
import com.example.mealkit.domain.Orders;

public interface CancelReturnRepository extends JpaRepository<CancelReturn, Long> {
	
	//특정 회원의 취소반품정보 조회
	List<CancelReturn> findByMemberId(Long memberId);
	
	//관리자 페이지 모든 회원 취소반품내역 조회
	List<CancelReturn> findAllByOrderByCreatedAtDesc();
	
	//주문 객체로 구매 취소 조회
	CancelReturn findByOrders(Orders order);
	
	//주문번호로 구매취소 내역 체크
	boolean existsByOrders(Orders order);
	
	//주문 번호로 구매 취소 조회
	CancelReturn findByOrders_Id(Long orderId);
	
	//추가 - 주문내역을 최근날짜로
	List<CancelReturn> findByMemberIdOrderByCreatedAtDesc(Long memberId);

	
}
