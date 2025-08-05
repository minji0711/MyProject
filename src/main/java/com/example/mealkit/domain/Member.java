package com.example.mealkit.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)	//db에서 중복 방지
	private String email;
	private String name;
	private String password;
	private String phone;
	private String gender;
	private String birthday;
	private boolean isAdmin;
	private LocalDateTime createdAt;
	
	@PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	private List<Product> products;
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Destination> destinations;
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Cart> carts;
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Wish> wishes;
	
	@OneToMany(mappedBy = "member")
	private List<Orders> orders;
	
	@OneToMany(mappedBy = "member")
	private List<Delivery> deliveries;
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	private List<Mcoupon> mcoupons;
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	private List<Mpoint> mpoints;
	
	@OneToMany(mappedBy = "member")
	private List<Review> reviews;
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	private List<Qna> qnas;
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	private List<CancelReturn> cancelReturns;
	
	
}
