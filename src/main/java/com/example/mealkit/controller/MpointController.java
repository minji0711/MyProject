package com.example.mealkit.controller;

import java.util.List;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.Mpoint;
import com.example.mealkit.dto.MpointAdminListDto;
import com.example.mealkit.dto.MpointDto;
import com.example.mealkit.dto.MpointListDto;
import com.example.mealkit.dto.MpointUseRequestDto;
import com.example.mealkit.repository.MpointRepository;

import jakarta.servlet.http.HttpSession;

import com.example.mealkit.repository.MemberRepository;

import lombok.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mpoint")
public class MpointController {
	
	private final MpointRepository mpointRepository;
	private final MemberRepository memberRepository;
	
	
	// **관리자: 회원 포인트 리스트
	@GetMapping("/admin")
	public ResponseEntity<?> listAdminMpoint(HttpSession session){
		Member loginUser = (Member) session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		if (!loginUser.isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한으로 로그인 하세요."); 
		}
		
		List<MpointAdminListDto> listdto = mpointRepository.findTotalPointWithMemberInfo();
		
		return ResponseEntity.ok(listdto);
			
	}
	
	
	// **멤버 포인트 지급 (관리자)
	@PostMapping("/admin")
	public ResponseEntity<String> addMpoint(@RequestBody MpointDto dto, HttpSession session) {
		//수정 추가
	    Member loginUser = (Member) session.getAttribute("loginMember");
	    if (loginUser == null || !loginUser.isAdmin()) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한 필요");
	    }
	    
		List<Member> members = memberRepository.findAllById(dto.getMemberIds());
		
		List<Mpoint> mpoints = members.stream()
				.map(member -> {
					Mpoint mpoint = new Mpoint();
					mpoint.setMember(member);
					mpoint.setPoint(dto.getPoint());
					mpoint.setDescription(dto.getDescription());
					return mpoint;		
				})
				.toList();
		mpointRepository.saveAll(mpoints);	
		return ResponseEntity.ok("포인트 지급을 완료하였습니다.");		
	}
	
	
	// **마이페이지 > 포인트 조회(type: 전체(all), 적립(saved), 사용포인트(used))
	@GetMapping("/{type}")
	public ResponseEntity<Object> listMpoint(@PathVariable String type, HttpSession session){
		Member loginUser = (Member)session.getAttribute("loginMember");
		
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");			
		}
		
		List<Mpoint> lists;
		List<MpointListDto> dtoList;
		
		switch (type.toLowerCase()) {
	
			case "all" :
				lists = mpointRepository.findAllByMemberIdOrderByCreatedAtDesc(loginUser.getId());
				
				dtoList = lists.stream()
						.map(list ->  new MpointListDto(
							list.getCreatedAt(),
							list.getPoint() > 0 ? "적립" : "사용" ,
					        list.getDescription(), 
					        list.getPoint() + "P"	
					        ))
						.toList();	
				break;
							
			case "saved" :
				lists = mpointRepository.findByMemberIdAndPointGreaterThanOrderByCreatedAtDesc(loginUser.getId(), 0);
				
				dtoList = lists.stream()
						.map(list -> new MpointListDto(
								list.getCreatedAt(), 
								"적립", 
								list.getDescription(),
								list.getPoint() + "P"
								))
						.toList();
				break;
				
			case "used" :
				lists = mpointRepository.findByMemberIdAndPointLessThanOrderByCreatedAtDesc(loginUser.getId(), 0);
				
				dtoList = lists.stream()
						.map(list -> new MpointListDto(
								list.getCreatedAt(), 
								"사용", 
								list.getDescription(),
								list.getPoint() + "P"								
								))
						.toList();
				break;
				
			default:
	            return ResponseEntity.badRequest().body("잘못된 type 값입니다.");
		}	
		return ResponseEntity.ok(dtoList);
	}
	
	
	// **마이페이지 > 보유 포인트 조회
	@GetMapping
	public ResponseEntity<?> getTotPoint(HttpSession session){
		Member loginUser = (Member)session.getAttribute("loginMember");
		
		int totPoint = mpointRepository.findTotalPointByMemberId(loginUser.getId());
		
		return ResponseEntity.ok(totPoint);			
	}
	
	
	
	// 포인트 사용
	@PostMapping("/orders")
	public ResponseEntity<?> useMpoint(@RequestBody MpointUseRequestDto dto, HttpSession session){

	    Member loginUser = (Member) session.getAttribute("loginMember");

	    if (loginUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");		
	    }

	    // 현재 보유포인트 조회
	    Integer totMpoint = mpointRepository.findTotalPointByMemberId(loginUser.getId());

	    if(totMpoint < dto.getUsePoint()) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용가능 포인트가 부족합니다.");
	    }

	    // 차감기록 저장 (음수 저장)
	    Mpoint mpoint = new Mpoint();
	    mpoint.setMember(loginUser);
	    mpoint.setPoint(-dto.getUsePoint());
	    mpoint.setDescription("상품구매 차감");
	    
	    // 주문번호 저장 (선택사항)
	    // 만약 주문 ID를 연동하려면 Orders 테이블에서 찾아서 넣을 수 있음
	    // Orders order = ordersRepository.findById(dto.getOrderId()).orElseThrow();
	    // mpoint.setOrders(order);

	    mpointRepository.save(mpoint);

	    return ResponseEntity.ok("포인트 사용이 정상 처리되었습니다.");
	}

	
	//추가
	@PostMapping("/refund")
	public ResponseEntity<?> refundMpoint(@RequestParam Long memberId, @RequestParam int refundPoint) {

	    Member member = memberRepository.findById(memberId)
	            .orElseThrow(() -> new RuntimeException("회원정보 없음"));

	    Mpoint mpoint = new Mpoint();
	    mpoint.setMember(member);
	    mpoint.setPoint(refundPoint);
	    mpoint.setDescription("주문취소 포인트 반환");

	    mpointRepository.save(mpoint);

	    return ResponseEntity.ok("포인트 반환이 정상 처리되었습니다.");
	}

}
