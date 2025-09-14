package com.growcorehub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.growcorehub.dto.response.AssessmentResponse;
import com.growcorehub.dto.response.ProjectResponse;
import com.growcorehub.entity.Project;
import com.growcorehub.entity.ProjectApplication;
import com.growcorehub.entity.User;
import com.growcorehub.enums.ApplicationStatus;
import com.growcorehub.enums.ProjectStatus;
import com.growcorehub.exception.ResourceNotFoundException;
import com.growcorehub.exception.BadRequestException;
import com.growcorehub.repository.ProjectApplicationRepository;
import com.growcorehub.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final ProjectApplicationRepository applicationRepository;
	private final UserService userService;
	private final AssessmentService assessmentService;
	private final ObjectMapper objectMapper;

	/**
	 * Get all active projects with optional category filter
	 */
	public Page<ProjectResponse> getAllActiveProjects(String category, Pageable pageable) {
		Page<Project> projects;

		if (category != null && !category.trim().isEmpty()) {
			projects = projectRepository.findByStatusAndCategory(ProjectStatus.ACTIVE, category.trim(), pageable);
		} else {
			projects = projectRepository.findByStatus(ProjectStatus.ACTIVE, pageable);
		}

		return projects.map(this::convertToProjectResponse);
	}

	/**
	 * Get all active projects with user-specific information
	 */
	public Page<ProjectResponse> getAllActiveProjects(String category, String userEmail, Pageable pageable) {
		Page<Project> projects;

		if (category != null && !category.trim().isEmpty()) {
			projects = projectRepository.findByStatusAndCategory(ProjectStatus.ACTIVE, category.trim(), pageable);
		} else {
			projects = projectRepository.findByStatus(ProjectStatus.ACTIVE, pageable);
		}

		User user = getUserIfExists(userEmail);
		final User finalUser = user;

		return projects.map(project -> convertToProjectResponse(project, finalUser));
	}

	/**
	 * Get project by ID with user-specific information
	 */
	public ProjectResponse getProjectById(Long id, String userEmail) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

		if (project.getStatus() != ProjectStatus.ACTIVE) {
			throw new BadRequestException("Project is not active");
		}

		User user = getUserIfExists(userEmail);
		return convertToProjectResponse(project, user);
	}

	/**
	 * Get all applications for a specific user
	 */
	public List<ProjectResponse> getUserApplications(String userEmail) {
		User user = userService.findByEmail(userEmail);
		List<ProjectApplication> applications = applicationRepository.findByUserId(user.getId());

		return applications.stream().map(app -> {
			ProjectResponse response = convertToProjectResponse(app.getProject(), user);
			response.setApplicationStatus(app.getApplicationStatus());
			response.setAppliedAt(app.getAppliedAt());
			response.setAssessmentScore(app.getAssessmentScore());
			return response;
		}).collect(Collectors.toList());
	}

	/**
	 * Get applications by status for a user
	 */
	public List<ProjectResponse> getUserApplicationsByStatus(String userEmail, ApplicationStatus status) {
		User user = userService.findByEmail(userEmail);
		List<ProjectApplication> applications = applicationRepository.findByUserIdAndApplicationStatus(user.getId(),
				status);

		return applications.stream().map(app -> convertToProjectResponse(app.getProject(), user))
				.collect(Collectors.toList());
	}

	/**
	 * Check if user can apply to project
	 */
	public boolean canUserApplyToProject(String userEmail, Long projectId) {
		try {
			User user = userService.findByEmail(userEmail);

			// Check if project exists and is active
			Project project = projectRepository.findById(projectId)
					.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

			if (project.getStatus() != ProjectStatus.ACTIVE) {
				return false;
			}

			// Check if user already applied
			return !applicationRepository.existsByUserIdAndProjectId(user.getId(), projectId);
		} catch (Exception e) {
			log.error("Error checking if user can apply to project: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Apply to project
	 */
	@Transactional
	public ProjectApplication applyToProject(String userEmail, Long projectId) {
		User user = userService.findByEmail(userEmail);

		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

		if (project.getStatus() != ProjectStatus.ACTIVE) {
			throw new BadRequestException("Project is not active for applications");
		}

		// Check if user already applied
		if (applicationRepository.existsByUserIdAndProjectId(user.getId(), projectId)) {
			throw new BadRequestException("You have already applied to this project");
		}

		// Check if user profile is completed
		if (user.getProfile() == null || !user.getProfile().getProfileCompleted()) {
			throw new BadRequestException("Please complete your profile before applying to projects");
		}

		// Create application
		ProjectApplication application = new ProjectApplication();
		application.setUser(user);
		application.setProject(project);
		application.setApplicationStatus(ApplicationStatus.APPLIED);

		ProjectApplication savedApplication = applicationRepository.save(application);
		log.info("User {} applied to project {}", userEmail, projectId);

		return savedApplication;
	}

	/**
	 * Get project statistics
	 */
	public ProjectStats getProjectStats(Long projectId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

		List<ProjectApplication> applications = applicationRepository.findByProjectId(projectId);

		long totalApplications = applications.size();
		long acceptedApplications = applications.stream()
				.filter(app -> app.getApplicationStatus() == ApplicationStatus.ACCEPTED).count();
		long completedApplications = applications.stream()
				.filter(app -> app.getApplicationStatus() == ApplicationStatus.COMPLETED).count();

		return new ProjectStats(totalApplications, acceptedApplications, completedApplications);
	}

	// Private helper methods

	private User getUserIfExists(String userEmail) {
		if (userEmail == null)
			return null;

		try {
			return userService.findByEmail(userEmail);
		} catch (ResourceNotFoundException e) {
			log.warn("User not found with email: {}", userEmail);
			return null;
		}
	}

	private ProjectResponse convertToProjectResponse(Project project) {
		return convertToProjectResponse(project, null);
	}

	private ProjectResponse convertToProjectResponse(Project project, User user) {
		ProjectResponse response = new ProjectResponse();
		response.setId(project.getId());
		response.setTitle(project.getTitle());
		response.setDescription(project.getDescription());
		response.setCategory(project.getCategory());
		response.setTermsConditions(project.getTermsConditions());
		response.setScopeOfWork(project.getScopeOfWork());
		response.setMinimumScore(project.getMinimumScore());
		response.setStatus(project.getStatus());
		response.setCreatedAt(project.getCreatedAt());
		response.setClientCrmUrl(project.getClientCrmUrl());

		// Parse required skills from JSON
		parseRequiredSkills(project, response);

		// Set user-specific information
		if (user != null) {
			setUserSpecificInfo(project, user, response);
		}

		// Get assessments
		List<AssessmentResponse> assessments = assessmentService.getAssessmentsByProjectId(project.getId(),
				user != null ? user.getId() : null);
		response.setAssessments(assessments);

		return response;
	}

	private void parseRequiredSkills(Project project, ProjectResponse response) {
		try {
			if (project.getRequiredSkills() != null && !project.getRequiredSkills().trim().isEmpty()) {
				List<String> skills = objectMapper.readValue(project.getRequiredSkills(),
						new TypeReference<List<String>>() {
						});
				response.setRequiredSkills(skills);
			}
		} catch (Exception e) {
			log.error("Error parsing required skills for project {}: {}", project.getId(), e.getMessage());
			// Set empty list as fallback
			response.setRequiredSkills(List.of());
		}
	}

	private void setUserSpecificInfo(Project project, User user, ProjectResponse response) {
		// Check if user has applied
		ProjectApplication application = applicationRepository.findByUserIdAndProjectId(user.getId(), project.getId())
				.orElse(null);

		if (application != null) {
			response.setHasApplied(true);
			response.setApplicationStatus(application.getApplicationStatus());
			response.setAppliedAt(application.getAppliedAt());
			response.setAssessmentScore(application.getAssessmentScore());
		} else {
			response.setHasApplied(false);
		}
	}

	// Inner class for project statistics
	public static class ProjectStats {
		private final long totalApplications;
		private final long acceptedApplications;
		private final long completedApplications;

		public ProjectStats(long totalApplications, long acceptedApplications, long completedApplications) {
			this.totalApplications = totalApplications;
			this.acceptedApplications = acceptedApplications;
			this.completedApplications = completedApplications;
		}

		// Getters
		public long getTotalApplications() {
			return totalApplications;
		}

		public long getAcceptedApplications() {
			return acceptedApplications;
		}

		public long getCompletedApplications() {
			return completedApplications;
		}
	}
}