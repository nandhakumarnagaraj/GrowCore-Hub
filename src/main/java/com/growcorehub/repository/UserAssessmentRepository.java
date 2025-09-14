package com.growcorehub.repository;

import com.growcorehub.entity.UserAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssessmentRepository extends JpaRepository<UserAssessment, Long> {
	
	List<UserAssessment> findByUserId(Long userId);

	Optional<UserAssessment> findByUserIdAndAssessmentId(Long userId, Long assessmentId);

	boolean existsByUserIdAndAssessmentId(Long userId, Long assessmentId);
}