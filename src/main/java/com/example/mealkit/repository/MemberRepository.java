package com.example.mealkit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mealkit.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Member findByEmail(String email);
	
	List<Member> findByIsAdminFalse();
	
	Optional<Member> findByName(String name);

	
}
