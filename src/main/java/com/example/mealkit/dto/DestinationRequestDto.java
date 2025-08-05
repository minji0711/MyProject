package com.example.mealkit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestinationRequestDto {

    private String destName;
    private String receiver;
    private String phone;
    private String zipcode;
    private String address;
    private String addressDetail;
    private boolean defaultAddress;
}
