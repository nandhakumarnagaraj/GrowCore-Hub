package com.growcorehub.dto.response;

import com.growcorehub.enums.ProjectStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectResponse {
	private Long id;
	private String title;
	private String description;
	private String category;
	private String termsConditions;
	private String scopeOfWork;
	private List<String> requiredSkills;
	private BigDecimal minimumScore;
	private ProjectStatus status;
	private LocalDateTime createdAt;
	private String clientCrmUrl;
	private Boolean hasApplied;
	private List<AssessmentResponse> assessments;
}