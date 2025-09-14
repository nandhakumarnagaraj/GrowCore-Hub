package com.growcorehub.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardResponse {
	private Long totalApplications;
	private Long acceptedApplications;
	private Long completedProjects;
	private BigDecimal totalHoursWorked;
	private BigDecimal averageScore;
	private List<ProjectResponse> recentProjects;
	private List<CertificationResponse> recentCertifications;
	private List<NotificationResponse> recentNotifications;
}

@Data
class CertificationResponse {
	private Long id;
	private String skillName;
	private BigDecimal score;
	private LocalDateTime earnedAt;
}

@Data
class NotificationResponse {
	private Long id;
	private String title;
	private String message;
	private Boolean isRead;
	private LocalDateTime createdAt;
}