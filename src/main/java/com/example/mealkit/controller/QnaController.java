package com.example.mealkit.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.mealkit.domain.Member;
import com.example.mealkit.domain.OrderItem;
import com.example.mealkit.domain.Qna;
import com.example.mealkit.dto.QnaDetailResponseDto;
import com.example.mealkit.dto.QnaRequestDto;
import com.example.mealkit.dto.QnaResponseDto;
import com.example.mealkit.repository.OrderItemRepository;
import com.example.mealkit.repository.QnaRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

	private final QnaRepository qnaRepository;
	private final OrderItemRepository orderItemRepository;
	@Value("${upload.path}")
    private String uploadPath;
	
	@PostMapping
	public ResponseEntity<?> createQna(@ModelAttribute QnaRequestDto dto,
										@RequestParam(required=false)MultipartFile image,
										HttpSession session) throws Exception{
		Member member = (Member) session.getAttribute("loginMember");
		if(member == null) return ResponseEntity.status(401).body("로그인 필요");	//보안
		
		Qna qna = new Qna();
		qna.setMember(member);
		qna.setCategory(dto.getCategory());
		qna.setTitle(dto.getTitle());
		qna.setContent(dto.getContent());
		
		//주문한 상품 관련 문의일때만 동작
		if(dto.getOrderItemId() != null) {
			OrderItem orderItem = orderItemRepository.findById(dto.getOrderItemId())
					.orElseThrow(() -> new IllegalArgumentException("주문 상품 없음"));
					qna.setOrderItem(orderItem);
		}
		//회원이 문의할때 사진을 올리면 처리 안올리면 무관
		if (image != null && !image.isEmpty()) {
			String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
			File dir = new File(uploadPath + "/qna");
			
			if (!dir.exists()) dir.mkdirs();
			File dest = new File(dir,filename);
			image.transferTo(dest);
			qna.setImagePath("/images/qna/" + filename);
		}
		qnaRepository.save(qna);
		return ResponseEntity.ok("문의 등록 완료");
	}
	@GetMapping
	public ResponseEntity<?> getMyQnas(HttpSession session) {
		Member member = (Member) session.getAttribute("loginMember");
		if (member == null) return ResponseEntity.status(401).body("로그인 필요");	//보안
		
		List<Qna> qnas = qnaRepository.findByMemberId(member.getId());
		//문의 목록 보기
		List<QnaResponseDto> response = qnas.stream().map(q -> {
			QnaResponseDto dto = new QnaResponseDto();
			dto.setId(q.getId());
			dto.setCategory(q.getCategory());
			dto.setTitle(q.getTitle());
			dto.setCreatedAt(q.getCreatedAt());
			dto.setAnswered(q.isAnswered());
			return dto;
		}).toList();
		return ResponseEntity.ok(response);
	}
	@GetMapping("/{id}")
	public ResponseEntity<?> getQnaDetail(@PathVariable Long id, HttpSession session){
			Member member = (Member) session.getAttribute("loginMember");
			if (member == null) return ResponseEntity.status(401).body("로그인 필요");
			
			Qna qna = qnaRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("문의 없음"));
			if (!qna.getMember().getId().equals(member.getId())) {
				return ResponseEntity.status(403).body("본임만 조회 가능");
			}
			QnaDetailResponseDto dto = new QnaDetailResponseDto();
			dto.setCategory(qna.getCategory());
			dto.setTitle(qna.getTitle());
			dto.setContent(qna.getContent());
			dto.setAnswer(qna.getAnswer());
			dto.setAnswered(qna.isAnswered());
			dto.setImagePath(qna.getImagePath());
			dto.setCreatedAt(qna.getCreatedAt());
			return ResponseEntity.ok(dto);
	}
	//문의는 수정없이 삭제만 가능한걸로 구현
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteQna(@PathVariable Long id, HttpSession session) {
		Member member = (Member) session.getAttribute("loginMember");
		if (member == null) return ResponseEntity.status(401).body("로그인 필요");
		
		Qna qna = qnaRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("문의없음"));
		
		if (!qna.getMember().getId().equals(member.getId())) {
			return ResponseEntity.status(403).body("본인만 삭제 가능");
		}
		
			if(qna.getImagePath() != null) {
				String filename = qna.getImagePath().replace("/images/qna/","");
				Path path = Paths.get(uploadPath + "/qna/" + filename);
				try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				e.printStackTrace();
				}
			}
		qnaRepository.delete(qna);
		return ResponseEntity.ok("삭제완료");
	}
	//수정
	@GetMapping("/admin")
	public ResponseEntity<?> getAllQna(HttpSession session) {
	    Member member = (Member) session.getAttribute("loginMember");
	    if (member == null || !member.isAdmin()) {
	        return ResponseEntity.status(403).body("관리자만 조회 가능");
	    }

	    List<Qna> qnas = qnaRepository.findAll();

	    List<QnaResponseDto> response = qnas.stream().map(q -> {
	        QnaResponseDto dto = new QnaResponseDto();
	        dto.setId(q.getId());
	        dto.setCategory(q.getCategory());
	        dto.setTitle(q.getTitle());
	        dto.setAnswered(q.isAnswered());
	        dto.setCreatedAt(q.getCreatedAt());
	        dto.setMemberName(q.getMember().getName()); // 작성자 이름 추가
	        return dto;
	    }).toList();

	    return ResponseEntity.ok(response);
	}

	//수정
	@PostMapping("/admin/answer/{id}")
	public ResponseEntity<?> answerQna(@PathVariable Long id, @RequestBody QnaRequestDto request) {
	    Qna qna = qnaRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("문의 없음"));
	    
	    qna.setAnswer(request.getAnswer());
	    qna.setAnswered(true);
	    qnaRepository.save(qna);
	    
	    return ResponseEntity.ok("답변 등록 완료");
	}

	
	//관리자 페이지에서 문의사항 세부보기 추가
	@GetMapping("/admin/{id}")
	public ResponseEntity<?> getQnaDetailAdmin(@PathVariable Long id, HttpSession session){
	    Member member = (Member) session.getAttribute("loginMember");
	    if (member == null || !member.isAdmin()) {
	        return ResponseEntity.status(403).body("관리자만 접근 가능");
	    }

	    Qna qna = qnaRepository.findById(id)
	        .orElseThrow(() -> new IllegalArgumentException("문의 없음"));

	    QnaDetailResponseDto dto = new QnaDetailResponseDto();
	    dto.setCategory(qna.getCategory());
	    dto.setTitle(qna.getTitle());
	    dto.setContent(qna.getContent());
	    dto.setAnswer(qna.getAnswer());
	    dto.setAnswered(qna.isAnswered());
	    dto.setImagePath(qna.getImagePath());
	    dto.setCreatedAt(qna.getCreatedAt());

	    return ResponseEntity.ok(dto);
	}
}
