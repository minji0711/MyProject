package com.example.mealkit.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Destination {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String destName;
	private String receiver;
	private String phone;
	private String zipCode;
	private String address;
	private String addressDetail;
	
	@Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean defaultAddress;

	// 회원과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnoreProperties({"destinations"})
    private Member member;
    
    @OneToMany(mappedBy = "destination")
    private List<Orders> orders;
	
}
