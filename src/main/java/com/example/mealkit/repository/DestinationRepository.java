package com.example.mealkit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mealkit.domain.Destination;


public interface DestinationRepository extends JpaRepository<Destination, Long>{

	List<Destination> findByMemberId(Long memberId);
	
    // 기본 배송지 조회 (Optional 사용)
    Optional<Destination> findByMemberIdAndDefaultAddressTrue(Long memberId);
    //기본배송지가 맨 위로 올라오게
    List<Destination> findByMemberIdOrderByDefaultAddressDescIdAsc(Long memberId);

}
