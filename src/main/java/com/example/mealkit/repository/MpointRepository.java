package com.example.mealkit.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mealkit.domain.Mpoint;
import com.example.mealkit.dto.MpointAdminListDto;

public interface MpointRepository extends JpaRepository<Mpoint, Long> {
	
	// 전체 포인트 사용내역
	List<Mpoint> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
	
	// 사용 포인트 내역(point: 음수인 경우)
	List<Mpoint> findByMemberIdAndPointLessThanOrderByCreatedAtDesc(Long memberId, int zero );
	
		
	// 적립 포인트 내역(point: 양수인 경우)
	List<Mpoint> findByMemberIdAndPointGreaterThanOrderByCreatedAtDesc(Long memberId, int zero);
	
	// 보유 포인트
	@Query("SELECT COALESCE(SUM(m.point), 0) FROM Mpoint m WHERE m.member.id = :memberId")
	int findTotalPointByMemberId(@Param("memberId") Long memberId);
	
	
	//관리자 : 회원별 보유 포인트 조회
	@Query("""
		    SELECT new com.example.mealkit.dto.MpointAdminListDto(
		        m.member.id,
		        m.member.name,
		        m.member.email,
		        m.member.createdAt,
		        COALESCE(SUM(m.point), 0)
		    )
		    FROM Mpoint m
		    GROUP BY m.member.id, m.member.name, m.member.email, m.member.createdAt
		""")
	List<MpointAdminListDto> findTotalPointWithMemberInfo();
	
	//추가
	@Query("SELECT COALESCE(SUM(m.point), 0) FROM Mpoint m WHERE m.member.id = :memberId")
	Integer getTotalPointByMemberId(@Param("memberId") Long memberId);

	
}
