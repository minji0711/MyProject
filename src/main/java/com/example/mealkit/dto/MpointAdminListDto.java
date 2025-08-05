package com.example.mealkit.dto;

import java.time.LocalDateTime;

public record  MpointAdminListDto(
	 Long MemberId,
	 String MemberName,
	 String MemberEmail,
	 LocalDateTime joinDate,
	 Long MemberPoint
){}


