package com.example.mealkit.controller;

import com.example.mealkit.domain.Destination;
import com.example.mealkit.domain.Member;
import com.example.mealkit.dto.DestinationRequestDto;
import com.example.mealkit.dto.DestinationResponseDto;
import com.example.mealkit.repository.DestinationRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationRepository destinationRepository;
   
    // 배송지 목록 조회
    @GetMapping
    public ResponseEntity<?> getDestinations(HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        List<DestinationResponseDto> list = destinationRepository.findByMemberIdOrderByDefaultAddressDescIdAsc(member.getId()).stream()
                .map(dest -> {
                    DestinationResponseDto dto = new DestinationResponseDto();
                    dto.setId(dest.getId());
                    dto.setDestName(dest.getDestName());
                    dto.setReceiver(dest.getReceiver());
                    dto.setPhone(dest.getPhone());
                    dto.setZipcode(dest.getZipCode());
                    dto.setAddress(dest.getAddress());
                    dto.setAddressDetail(dest.getAddressDetail());
                    dto.setDefaultAddress(dest.isDefaultAddress());
                    return dto;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    // 배송지 등록
    @PostMapping
    public ResponseEntity<?> addDestination(@RequestBody DestinationRequestDto dto, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        List<Destination> existing = destinationRepository.findByMemberId(member.getId());

        // 10개 초과 시 오래된 주소 삭제
        if (existing.size() >= 10) {
            Destination oldest = existing.stream()
                    .min(Comparator.comparing(Destination::getId)) // 가장 오래된 것 기준
                    .filter(dest -> !dest.isDefaultAddress()) // 기본 배송지는 삭제 불가
                    .orElse(null);

            if (oldest != null) destinationRepository.delete(oldest);
        }

        // 기본 배송지 지정 시 기존 기본 배송지 해제
        if (dto.isDefaultAddress()) {
            destinationRepository.findByMemberIdAndDefaultAddressTrue(member.getId()).ifPresent(dest -> {
                dest.setDefaultAddress(false);
                destinationRepository.save(dest);
            });
        }

        Destination newDest = new Destination();
        newDest.setMember(member);
        newDest.setDestName(dto.getDestName());
        newDest.setReceiver(dto.getReceiver());
        newDest.setPhone(dto.getPhone());
        newDest.setZipCode(dto.getZipcode());
        newDest.setAddress(dto.getAddress());
        newDest.setAddressDetail(dto.getAddressDetail());
        newDest.setDefaultAddress(dto.isDefaultAddress());
        
        destinationRepository.save(newDest);

        return ResponseEntity.ok("배송지 등록 완료");
    }
    // 배송지 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDestination(@PathVariable Long id, @RequestBody DestinationRequestDto dto, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        Optional<Destination> optional = destinationRepository.findById(id);
        if (optional.isEmpty() || !optional.get().getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        Destination dest = optional.get();

        // 기본 배송지 설정 변경
        if (dto.isDefaultAddress() && !dest.isDefaultAddress()) {
            destinationRepository.findByMemberIdAndDefaultAddressTrue(member.getId()).ifPresent(existingDefault -> {
                existingDefault.setDefaultAddress(false);
                destinationRepository.save(existingDefault);
            });
            dest.setDefaultAddress(true);
        }

        dest.setDestName(dto.getDestName());
        dest.setReceiver(dto.getReceiver());
        dest.setPhone(dto.getPhone());
        dest.setZipCode(dto.getZipcode());
        dest.setAddress(dto.getAddress());
        dest.setAddressDetail(dto.getAddressDetail());

        destinationRepository.save(dest);
        return ResponseEntity.ok("배송지 수정 완료");
    }
    
    // 배송지 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDestination(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        Optional<Destination> optional = destinationRepository.findById(id);
        if (optional.isEmpty() || !optional.get().getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        Destination dest = optional.get();
        if (dest.isDefaultAddress()) {
            return ResponseEntity.badRequest().body("기본 배송지는 삭제할 수 없습니다.");
        }

        destinationRepository.delete(dest);
        return ResponseEntity.ok("배송지 삭제 완료");
    }

    
}
