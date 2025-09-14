package com.growcorehub.dto.request;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
	private String aadhaarNumber;
	private String education;
	private String skills;
	private Integer experienceYears;
	private String phone;
}