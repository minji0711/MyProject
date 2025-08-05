package com.example.mealkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mealkit.domain.Product;
import com.example.mealkit.dto.ProductRatingDto;

public interface ProductRepository extends JpaRepository<Product, Long>{
	
	List<Product> findByMemberId(Long memberId);

	List<Product> findAllByOrderByIdDesc();
	
	//일반 회원 상품 조회
	@Query("""
		    SELECT p FROM Product p
		    WHERE (:category IS NULL OR p.category = :category)
		      AND (:category2 IS NULL OR p.category2 = :category2)
		    ORDER BY p.price ASC
		""")
		List<Product> findByCategoryFilterOrderByPriceAsc(
		    @Param("category") String category,
		    @Param("category2") String category2
		);

	@Query("""
		    SELECT p FROM Product p
		    WHERE (:category IS NULL OR p.category = :category)
		      AND (:category2 IS NULL OR p.category2 = :category2)
		    ORDER BY p.price DESC
		""")
		List<Product> findByCategoryFilterOrderByPriceDesc(
		    @Param("category") String category,
		    @Param("category2") String category2
		);

	@Query("""
		    SELECT p FROM Product p
		    WHERE (:category IS NULL OR p.category = :category)
		      AND (:category2 IS NULL OR p.category2 = :category2)
		    ORDER BY p.id DESC
		""")
		List<Product> findByCategoryFilterOrderByIdDesc(
		    @Param("category") String category,
		    @Param("category2") String category2
		);

	// 리뷰별점순 상품 정렬하기 (카테고리정렬)
    @Query("""
	    SELECT new com.example.mealkit.dto.ProductRatingDto(p, COALESCE(AVG(r.score), 0))
	    FROM Product p
	    LEFT JOIN Review r ON p.id = r.product.id
	    WHERE (:category1 IS NULL OR p.category = :category)
	      AND (:category2 IS NULL OR p.category2 = :category2)
	    GROUP BY p.id
	    ORDER BY AVG(r.score) DESC
	""")
	List<ProductRatingDto> findByCategoryAndOrderByRatingDesc(
	    @Param("category1") String category1,
	    @Param("category2") String category2
	);


	List<Product> findByCategory(String category); // 카테고리별 조회
	List<Product> findByCategory2(String category2); // 세부 카테고리

	//검색기능 구현 시 (상품페이지말고 메인페이지에서 검색창이 있음)
	List<Product> findByNameContaining(String keyword);
	
	//추가
	// ✅ 리뷰 평균평점 기준 내림차순 정렬 (리뷰순 정렬)
    @Query("""
        SELECT p 
        FROM Product p 
        LEFT JOIN Review r ON p.id = r.product.id 
        WHERE (:category IS NULL OR p.category = :category)
        GROUP BY p
        ORDER BY COALESCE(AVG(r.score), 0) DESC
    """)
    List<Product> findProductsOrderByRatingDesc(@Param("category") String category);
    
   
}
