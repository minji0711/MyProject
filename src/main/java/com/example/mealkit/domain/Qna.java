package com.example.mealkit.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Qna {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String category;
	private String title;
	private String content;
	private String answer;
	private boolean answered;
	private String imagePath;
	
	private LocalDateTime createdAt;
	@PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orderItem_id")
	private OrderItem orderItem;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	@JsonIgnoreProperties({"qnas"})
	private Member member;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

}
