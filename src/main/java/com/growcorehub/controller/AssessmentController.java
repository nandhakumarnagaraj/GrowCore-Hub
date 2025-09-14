package com.growcorehub.controller;

import com.growcorehub.dto.request.AssessmentSubmissionRequest;
import com.growcorehub.dto.response.AssessmentResponse;
import com.growcorehub.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AssessmentController {

	private final AssessmentService assessmentService;

	@GetMapping("/project/{projectId}")
	public ResponseEntity<List<AssessmentResponse>> getAssessmentsByProject(@PathVariable Long projectId,
			Authentication authentication) {

		// This would typically require user authentication to get user-specific data
		List<AssessmentResponse> assessments = assessmentService.getAssessmentsByProjectId(projectId, null);
		return ResponseEntity.ok(assessments);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AssessmentResponse> getAssessmentById(@PathVariable Long id, Authentication authentication) {

		AssessmentResponse assessment = assessmentService.getAssessmentById(id, authentication.getName());
		return ResponseEntity.ok(assessment);
	}

	@PostMapping("/{id}/submit")
	public ResponseEntity<AssessmentResponse> submitAssessment(@PathVariable Long id,
			@Valid @RequestBody AssessmentSubmissionRequest request, Authentication authentication) {

		request.setAssessmentId(id);
		AssessmentResponse response = assessmentService.submitAssessment(request, authentication.getName());
		return ResponseEntity.ok(response);
	}
}