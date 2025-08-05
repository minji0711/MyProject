package com.example.mealkit.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class Mpoint {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private int point;
	private LocalDateTime createdAt;	
	private String description;
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id")
	private Orders orders;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
	@JsonIgnoreProperties({"mpoints"})  //요기
    private Member member;
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;
	
	@PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
		

}
