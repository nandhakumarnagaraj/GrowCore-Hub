package com.growcorehub.repository;

import com.growcorehub.entity.ProjectApplication;
import com.growcorehub.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
	
	List<ProjectApplication> findByUserId(Long userId);

	List<ProjectApplication> findByProjectId(Long projectId);

	Optional<ProjectApplication> findByUserIdAndProjectId(Long userId, Long projectId);

	boolean existsByUserIdAndProjectId(Long userId, Long projectId);

	List<ProjectApplication> findByUserIdAndApplicationStatus(Long userId, ApplicationStatus status);
}