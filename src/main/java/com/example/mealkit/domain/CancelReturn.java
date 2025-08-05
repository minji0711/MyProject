package com.example.mealkit.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class CancelReturn {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDateTime createdAt;	
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", unique = true)
    private Orders orders;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
	@JsonIgnoreProperties({"cancelReturns"})
    private Member member;						//취소한 사용자
	
	@PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
	
}
