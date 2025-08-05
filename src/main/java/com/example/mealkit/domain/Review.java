package com.example.mealkit.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class Review {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private int score;
	private String content;
	private String imagePath;
	private String imagePath2;
	@Column(updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@OneToOne
	@JoinColumn(name = "order_Item_id", unique = true)
    private OrderItem orderItem;

	@ManyToOne
	@JoinColumn(name = "member_id")
	@JsonIgnoreProperties({"reviews"})
	private Member member;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	@JsonIgnoreProperties({"reviews"})
	private Product product;
	
	
	@PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
	
}
