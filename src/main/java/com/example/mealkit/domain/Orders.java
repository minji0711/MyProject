package com.example.mealkit.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class Orders {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private int amount;
	private int point;
	private int payment;
	private String paymentMethod;
	private int deliveryFee;
	private LocalDateTime createdAt;
	private String deliveryRequest;
	private int discountCoupon;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	@JsonIgnoreProperties({"orders"})
	private Member member;
	
	@OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems = new ArrayList<>();
	
	@OneToOne(mappedBy = "orders", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private CancelReturn cancelReturn;
		
	@OneToOne(fetch = FetchType.LAZY,mappedBy = "orders", cascade = CascadeType.ALL)
	@JoinColumn(name="delivery_id")
	private Delivery delivery;
	
	@PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "destination_id")
	private Destination destination;
	
	@OneToOne(mappedBy = "orders", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Mcoupon mcoupon;
	
	
}
