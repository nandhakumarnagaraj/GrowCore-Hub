package com.growcorehub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectApplicationRequest {
	@NotNull
	private Long projectId;

	@NotNull
	private Long assessmentId;
}