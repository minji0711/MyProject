package com.example.mealkit.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter 
@NoArgsConstructor
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private int quantity;
	private int price;
	
	
	@ManyToOne
	@JoinColumn(name = "orders_id")
	@JsonIgnoreProperties({"orderItems"})
	private Orders orders;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	@JsonIgnoreProperties({"orderItems"})
	private Product product;
	
	@OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL)
    private Review review;
	
	
	public OrderItem(int quantity, int price, Orders order, Product product) {
		this.quantity = quantity;
		this.price = price;
		this.orders = order;
		this.product = product;
	}
			
}
