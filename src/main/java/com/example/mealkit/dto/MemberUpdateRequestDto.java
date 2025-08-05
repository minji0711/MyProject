package com.example.mealkit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberUpdateRequestDto {

	private Long id;
	private String phone;
	private String password;
	private String newPassword;
	private String name;
	private String gender;
	private String birthday;
}
