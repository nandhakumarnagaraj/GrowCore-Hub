package com.growcorehub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentSubmissionRequest {
	@NotNull
	private Long assessmentId;

	@NotNull
	private String answers; // JSON string
}