package com.growcorehub.controller;

import com.growcorehub.dto.request.ProjectApplicationRequest;
import com.growcorehub.dto.response.ProjectResponse;
import com.growcorehub.entity.ProjectApplication;
import com.growcorehub.entity.User;
import com.growcorehub.enums.ApplicationStatus;
import com.growcorehub.exception.BadRequestException;
import com.growcorehub.exception.ResourceNotFoundException;
import com.growcorehub.repository.ProjectApplicationRepository;
import com.growcorehub.repository.ProjectRepository;
import com.growcorehub.service.EmailService;
import com.growcorehub.service.NotificationService;
import com.growcorehub.service.ProjectService;
import com.growcorehub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectController {

	private final ProjectService projectService;
	private final ProjectApplicationRepository applicationRepository;
	private final ProjectRepository projectRepository;
	private final UserService userService;
	private final NotificationService notificationService;
	private final EmailService emailService;

	@GetMapping
	public ResponseEntity<Page<ProjectResponse>> getAllProjects(@RequestParam(required = false) String category,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir, Authentication authentication) {

		Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		String userEmail = authentication != null ? authentication.getName() : null;
		Page<ProjectResponse> projects = projectService.getAllActiveProjects(category, userEmail, pageable);

		return ResponseEntity.ok(projects);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id, Authentication authentication) {

		String userEmail = authentication != null ? authentication.getName() : null;
		ProjectResponse project = projectService.getProjectById(id, userEmail);
		return ResponseEntity.ok(project);
	}

	@PostMapping("/{id}/apply")
	public ResponseEntity<String> applyToProject(@PathVariable Long id, Authentication authentication) {

		User user = userService.findByEmail(authentication.getName());

		// Check if project exists
		if (!projectRepository.existsById(id)) {
			throw new ResourceNotFoundException("Project not found with id: " + id);
		}

		// Check if user already applied
		if (applicationRepository.existsByUserIdAndProjectId(user.getId(), id)) {
			throw new BadRequestException("You have already applied to this project");
		}

		// Create application
		ProjectApplication application = new ProjectApplication();
		application.setUser(user);
		application.setProject(projectRepository.findById(id).get());
		application.setApplicationStatus(ApplicationStatus.APPLIED);
		applicationRepository.save(application);

		// Send notification and email
		String projectTitle = projectRepository.findById(id).get().getTitle();
		emailService.sendApplicationNotification(user.getEmail(), projectTitle);

		return ResponseEntity.ok("Application submitted successfully");
	}

	@GetMapping("/my-applications")
	public ResponseEntity<List<ProjectResponse>> getMyApplications(Authentication authentication) {
		List<ProjectResponse> applications = projectService.getUserApplications(authentication.getName());
		return ResponseEntity.ok(applications);
	}
}