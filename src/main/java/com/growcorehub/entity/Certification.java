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
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Certification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "skill_name", nullable = false)
	private String skillName;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal score;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assessment_id")
	private Assessment assessment;

	@CreatedDate
	@Column(name = "earned_at", updatable = false)
	private LocalDateTime earnedAt;
}