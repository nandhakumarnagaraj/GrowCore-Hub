package com.growcorehub.entity;

import com.growcorehub.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "aadhaar_number", length = 12)
	private String aadhaarNumber;

	@Column(length = 500)
	private String education;

	@Column(columnDefinition = "TEXT")
	private String skills;

	@Column(name = "experience_years")
	private Integer experienceYears = 0;

	@Column(name = "profile_completed")
	private Boolean profileCompleted = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status")
	private VerificationStatus verificationStatus = VerificationStatus.PENDING;

	@Column(name = "verification_documents", columnDefinition = "JSON")
	private String verificationDocuments;
}