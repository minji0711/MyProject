package com.example.mealkit.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String gender;
    private String birthday;
    private LocalDateTime createdAt;
    private boolean isAdmin;
}
