package com.growcorehub.entity;

import com.growcorehub.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Project {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	private String category;

	@Column(name = "terms_conditions", columnDefinition = "TEXT")
	private String termsConditions;

	@Column(name = "scope_of_work", columnDefinition = "TEXT")
	private String scopeOfWork;

	@Column(name = "required_skills", columnDefinition = "JSON")
	private String requiredSkills;

	@Column(name = "minimum_score", precision = 5, scale = 2)
	private BigDecimal minimumScore = new BigDecimal("70.00");

	@Enumerated(EnumType.STRING)
	private ProjectStatus status = ProjectStatus.ACTIVE;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "client_crm_url", length = 500)
	private String clientCrmUrl;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Assessment> assessments;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ProjectApplication> applications;
}