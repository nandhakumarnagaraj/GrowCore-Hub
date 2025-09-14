package com.growcorehub.entity;

import com.growcorehub.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProjectApplication {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Enumerated(EnumType.STRING)
	@Column(name = "application_status")
	private ApplicationStatus applicationStatus = ApplicationStatus.APPLIED;

	@Column(name = "assessment_score", precision = 5, scale = 2)
	private BigDecimal assessmentScore;

	@CreatedDate
	@Column(name = "applied_at", updatable = false)
	private LocalDateTime appliedAt;

	@Column(name = "agreement_signed")
	private Boolean agreementSigned = false;

	@Column(name = "agreement_signed_at")
	private LocalDateTime agreementSignedAt;
}