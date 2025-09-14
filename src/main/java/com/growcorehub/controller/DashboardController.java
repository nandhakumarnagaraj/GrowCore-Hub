package com.growcorehub.controller;

import com.growcorehub.dto.response.DashboardResponse;
import com.growcorehub.entity.*;
import com.growcorehub.enums.ApplicationStatus;
import com.growcorehub.repository.*;
import com.growcorehub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

	private final UserService userService;
	private final ProjectApplicationRepository applicationRepository;
	private final WorkSessionRepository workSessionRepository;
	private final UserAssessmentRepository userAssessmentRepository;
	private final CertificationRepository certificationRepository;
	private final NotificationRepository notificationRepository;

	@GetMapping("/summary")
	public ResponseEntity<DashboardResponse> getDashboardSummary(Authentication authentication) {
		User user = userService.findByEmail(authentication.getName());

		DashboardResponse response = new DashboardResponse();

		// Application statistics
		List<ProjectApplication> applications = applicationRepository.findByUserId(user.getId());
		response.setTotalApplications((long) applications.size());
		response.setAcceptedApplications(
				applications.stream().filter(app -> app.getApplicationStatus() == ApplicationStatus.ACCEPTED).count());
		response.setCompletedProjects(
				applications.stream().filter(app -> app.getApplicationStatus() == ApplicationStatus.COMPLETED).count());

		// Work hours (last 30 days)
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		BigDecimal totalHours = workSessionRepository.getTotalHoursWorkedByUserBetweenDates(user.getId(), thirtyDaysAgo,
				LocalDateTime.now());
		response.setTotalHoursWorked(totalHours);

		// Average score
		List<UserAssessment> userAssessments = userAssessmentRepository.findByUserId(user.getId());
		if (!userAssessments.isEmpty()) {
			BigDecimal averageScore = userAssessments.stream().map(UserAssessment::getScore)
					.reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(userAssessments.size()));
			response.setAverageScore(averageScore);
		} else {
			response.setAverageScore(BigDecimal.ZERO);
		}

		// Recent certifications
		List<Certification> recentCertifications = certificationRepository.findByUserIdOrderByEarnedAtDesc(user.getId())
				.stream().limit(5).collect(Collectors.toList());
		// Convert to response format would go here

		// Recent notifications
		List<Notification> recentNotifications = notificationRepository
				.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 5)).getContent();
		// Convert to response format would go here

		return ResponseEntity.ok(response);
	}
}