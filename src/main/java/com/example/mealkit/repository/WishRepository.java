package com.example.mealkit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mealkit.domain.Wish;

public interface WishRepository extends JpaRepository<Wish, Long>{
	
	List<Wish> findByMemberId(Long memberId);

	//찜목록 갯수는 최대 30개로 30개가 꽉차면 자동으로 오래된상품부터 삭제
	Optional<Wish> findTopByMemberIdOrderByIdAsc(Long memberId);

	//찜 버튼 누를 때 이걸로 먼저 중복 확인한 후, 없을 경우에만 저장
	boolean existsByMemberIdAndProductId(Long memberId, Long productId);

	
}
