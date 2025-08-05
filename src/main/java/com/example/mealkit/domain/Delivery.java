package com.example.mealkit.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class Delivery {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String status;			//READY : 배송준비중, COMPLETE : 배송완료
	private LocalDateTime createdAt;	
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orders_id")
	private Orders orders;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
	
	@PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
	
}
