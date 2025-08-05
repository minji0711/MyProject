package com.example.mealkit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSignupRequestDto {
    private String email;
    private String name;
    private String password;
    private String phone;
    private String gender;
    private String birthday;
}
