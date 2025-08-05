package com.example.mealkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mealkit.domain.Qna;

public interface QnaRepository extends JpaRepository<Qna, Long>{

	List<Qna> findByMemberId(Long memberId);
	
	// 최신순 전체 Q&A 조회 (관리자 페이지용)
    List<Qna> findAllByOrderByCreatedAtDesc();
    
    // 특정 상품에 대한 QnA 최신순 정렬
    List<Qna> findByProduct_IdOrderByCreatedAtDesc(Long productId);
}
