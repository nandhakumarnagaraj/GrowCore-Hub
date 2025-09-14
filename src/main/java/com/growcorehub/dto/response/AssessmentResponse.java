package com.growcorehub.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssessmentResponse {
	private Long id;
	private String name;
	private String description;
	private List<QuestionResponse> questions;
	private BigDecimal maxScore;
	private Integer timeLimitMinutes;
	private Boolean isCompleted;
	private BigDecimal userScore;
	private LocalDateTime completedAt;
}

@Data
class QuestionResponse {
	private String question;
	private List<String> options;
	private String type; // multiple-choice, text, etc.
}