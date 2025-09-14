package com.growcorehub.dto.response;

import com.growcorehub.enums.VerificationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
	private Long id;
	private String email;
	private String firstName;
	private String lastName;
	private String phone;
	private Boolean emailVerified;
	private LocalDateTime createdAt;

	// Profile fields
	private String aadhaarNumber;
	private String education;
	private String skills;
	private Integer experienceYears;
	private Boolean profileCompleted;
	private VerificationStatus verificationStatus;
}