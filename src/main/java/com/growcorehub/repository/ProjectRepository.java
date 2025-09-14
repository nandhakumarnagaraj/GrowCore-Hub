package com.growcorehub.repository;

import com.growcorehub.entity.Project;
import com.growcorehub.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
	
	Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

	@Query("SELECT p FROM Project p WHERE p.status = :status AND " + "(:category IS NULL OR p.category = :category)")
	Page<Project> findByStatusAndCategory(@Param("status") ProjectStatus status, @Param("category") String category,
			Pageable pageable);
}