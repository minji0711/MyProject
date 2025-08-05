package com.example.mealkit.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mealkit.domain.Review;
import com.example.mealkit.dto.ProductRatingDto;

public interface ReviewRepository extends JpaRepository<Review, Long> {
		
	// 특정 회원의 리뷰
	List<Review> findByMemberId(Long memberId);
	
	// 특정 주문 항목에 대한 리뷰 조회 (작성 여부 확인용)
	Optional<Review> findByOrderItemId(Long orderItemId); 
	
	// 상품에 대한 리뷰조회
	List<Review> findByProductId(Long productId);
	
    
    // 특정 회원이 작성한 리뷰 중 특정 상품에 대한 리뷰
	List<Review> findByMemberIdAndOrderItem_Product_Id(Long memberId, Long productId);
	
    // 리뷰가 존재하는 주문 ID 리스트를 조회해서 작성 가능한 것과 구분 가능
	boolean existsByOrderItemId(Long orderItemId);
	
	// 상품별 리뷰 평균 평점
	@Query("SELECT AVG(r.score) FROM Review r WHERE r.orderItem.product.id = :productId")
    Double findAverageScoreByProductId(Long productId);

    //상품별 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.orderItem.product.id = :productId")
    Long countByProductId(Long productId);
    
    
	// 리뷰별점순 상품 정렬하기 (카테고리정렬)
    @Query("""
	    SELECT new com.example.mealkit.dto.ProductRatingDto(p, COALESCE(AVG(r.score), 0))
	    FROM Product p
	    LEFT JOIN Review r ON p.id = r.product.id
	    WHERE (:category1 IS NULL OR p.category = :category1)
	      AND (:category2 IS NULL OR p.category2 = :category2)
	    GROUP BY p.id
	    ORDER BY AVG(r.score) DESC
	""")
	List<ProductRatingDto> findByCategoryAndOrderByRatingDesc(
	    @Param("category1") String category1,
	    @Param("category2") String category2
	);
	
    
    //관리자 리뷰목록 최신순으로
    List<Review> findAllByOrderByCreatedAtDesc();

}
