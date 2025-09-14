package com.growcorehub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_assessments", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "assessment_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserAssessment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assessment_id", nullable = false)
	private Assessment assessment;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal score;

	@Column(columnDefinition = "JSON")
	private String answers;

	@CreatedDate
	@Column(name = "completed_at", updatable = false)
	private LocalDateTime completedAt;
}