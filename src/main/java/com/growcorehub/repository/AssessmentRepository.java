package com.growcorehub.repository;

import com.growcorehub.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
	
	List<Assessment> findByProjectId(Long projectId);
}