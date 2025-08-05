package com.example.mealkit.controller;

import java.util.List;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mealkit.domain.Member;
import com.example.mealkit.dto.MemberCheckResponseDto;
import com.example.mealkit.dto.MemberLoginRequestDto;
import com.example.mealkit.dto.MemberResponseDto;
import com.example.mealkit.dto.MemberSignupRequestDto;
import com.example.mealkit.dto.MemberUpdateRequestDto;
import com.example.mealkit.repository.MemberRepository;
import com.example.mealkit.service.EmailService;
import com.example.mealkit.service.McouponService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final McouponService mcouponService;
    private final EmailService emailService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody MemberSignupRequestDto dto) {

        Member member = new Member();
        member.setEmail(dto.getEmail());
        member.setName(dto.getName());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setPhone(dto.getPhone());
        member.setGender(dto.getGender());
        member.setBirthday(dto.getBirthday());
        member.setAdmin(false);

        memberRepository.save(member);
        
        // 회원가입 쿠폰발행
        mcouponService.signupMcoupon(member);
        
        return ResponseEntity.ok("회원가입 성공");
    }
    @GetMapping("/checkEmail")	//회원가입 시 중복확인 버튼 누를 시
    public ResponseEntity<MemberCheckResponseDto> checkEmailDuplicate(@RequestParam String email) {
    	boolean exists = memberRepository.findByEmail(email) != null;
    	String message = exists ? "중복된 이메일입니다." : "사용 가능한 이메일입니다.";
    	
    	MemberCheckResponseDto response = new MemberCheckResponseDto(exists, message);
    	return ResponseEntity.ok(response);
    }
    
   
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginRequestDto dto, HttpSession session) {
        Member member = memberRepository.findByEmail(dto.getEmail());

        if (member == null || !passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            return ResponseEntity.status(401).body("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        session.setAttribute("loginMember", member);
        
        //로그인 후 > 쿠폰 조회 후 기간만료 처리(useYn=P) 
        Member loginMember = (Member)session.getAttribute("loginMember");
        mcouponService.checkExpireMcoupons(loginMember.getId());

        return ResponseEntity.ok("로그인 성공");
    }

    // 보안으로 본인확인
    @GetMapping("/me")
    public ResponseEntity<?> getLoginMember(HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");

        if (member == null) {
            return ResponseEntity.status(401).body("로그인되어 있지 않습니다.");
        }

        MemberResponseDto response = new MemberResponseDto();
        response.setId(member.getId());
        response.setEmail(member.getEmail());
        response.setName(member.getName());
        response.setPhone(member.getPhone());
        response.setGender(member.getGender());
        response.setBirthday(member.getBirthday());
        response.setAdmin(member.isAdmin());

        return ResponseEntity.ok(response);
    }

    /*@PutMapping("/update")
    public ResponseEntity<?> updateMember(@RequestBody MemberUpdateRequestDto dto,
    		HttpSession session){
    	Member sessionmember = (Member) session.getAttribute("loginMember");
    	
    	if (sessionmember == null) {
    		return ResponseEntity.status(401).body("로그인이 필요합니다.");
    	}
    	
    	Member member = memberRepository.findById(sessionmember.getId()).orElse(null);
    		
    		if (member == null) {
    			return ResponseEntity.status(404).body("회원 정보를 찾을 수 없습니다.");
    		}
    		if (dto.getPassword() != null && dto.getNewPassword() != null) {
    			
    			if (!passwordEncoder.matches(dto.getPassword(),member.getPassword())) {
    		
    		return ResponseEntity.status(400).body("기존 비밀번호가 아닙니다.");
    			}
    			
    		member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
	    	}
    		//개인정보수정 목록 회의하고 추가하기 (현재는 비밀번호와 핸드폰번호)
    		if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
    			member.setPhone(dto.getPhone());
    		}
	    	memberRepository.save(member);
	    	session.setAttribute("loginMember", member);
	    	
	    	return ResponseEntity.ok("회원정보를 수정했습니다");
	    }*/
    
    @PutMapping("/update")
    public ResponseEntity<?> updateMember(@RequestBody MemberUpdateRequestDto dto,
    		HttpSession session){
    	Member sessionmember = (Member) session.getAttribute("loginMember");
    	
    	if (sessionmember == null) {
    		return ResponseEntity.status(401).body("로그인이 필요합니다.");
    	}
    	
    	Member member = memberRepository.findById(sessionmember.getId()).orElse(null);
    		
    		if (member == null) {
    			return ResponseEntity.status(404).body("회원 정보를 찾을 수 없습니다.");
    		}
    		//기존비밀번호 확인 (수정 로직x)    	
    			if (!passwordEncoder.matches(dto.getPassword(),member.getPassword())) {
    		return ResponseEntity.status(400).body("기존 비밀번호가 아닙니다.");
	    	}
    			
    		//이름,핸드폰번호,성별,생일 수정
    		if (dto.getName() != null && !dto.getName().isBlank()) {
        			member.setName(dto.getName());
        	}
    		if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
    			member.setPhone(dto.getPhone());
    		}
    		if (dto.getGender() != null && !dto.getGender().isBlank()) {
    			member.setGender(dto.getGender());
    		}
    		if (dto.getBirthday() != null && !dto.getBirthday().isBlank()) {
    			member.setBirthday(dto.getBirthday());
    		}
	    	memberRepository.save(member);
	    	session.setAttribute("loginMember", member);
	    	
	    	return ResponseEntity.ok("회원정보를 수정했습니다");
	    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 완료");
    }
    
    //관리자 대시보드 (일단 총 회원가입수와 모든회원정보 리스트)
    @GetMapping("/admin/count")
    public ResponseEntity<?> getTotalMemberCount(HttpSession session) {
    	Member member = (Member) session.getAttribute("loginMember");
    	
    	if(member == null || !member.isAdmin()) {
    		return ResponseEntity.status(403).body("관리자만 접근 가능");
    	}
    	long count = memberRepository.count()-1;
    	return ResponseEntity.ok(Map.of("totalMemberCount", count));
    }
    
    @GetMapping("/admin/all")
    public ResponseEntity<List<MemberResponseDto>> getAllMembers() {
    		
    	List<Member> members = memberRepository.findByIsAdminFalse();
    	List<MemberResponseDto> dto = members.stream().map(
    			member -> new MemberResponseDto (
    					member.getId(),
    					member.getEmail(),
    					member.getName(),
    					member.getPhone(),
    					member.getGender(),
    					member.getBirthday(),
    					member.getCreatedAt(),
    					member.isAdmin()
    				)).toList();
    	return ResponseEntity.ok(dto);
    }   
   
    
    //추가
    @PostMapping("/reset-password-by-username")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String name = request.get("name");
        String phone = request.get("phone");

        // 이메일로 사용자 찾기
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            return ResponseEntity.status(404).body("해당 이메일로 가입된 계정이 없습니다.");
        }

        // 이름과 전화번호 확인
        if (!member.getName().equals(name) || !member.getPhone().equals(phone)) {
            return ResponseEntity.status(400).body("입력한 정보가 일치하지 않습니다.");
        }

        // 임시 비밀번호 생성 및 암호화 저장
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        member.setPassword(passwordEncoder.encode(tempPassword));
        memberRepository.save(member);

        // 이메일 발송
        emailService.sendTempPassword(email, tempPassword);

        return ResponseEntity.ok("임시 비밀번호가 이메일로 발송되었습니다.");
    }

}
