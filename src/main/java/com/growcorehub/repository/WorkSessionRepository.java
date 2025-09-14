package com.growcorehub.repository;

import com.growcorehub.entity.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {
	
	List<WorkSession> findByUserId(Long userId);

	List<WorkSession> findByUserIdAndProjectId(Long userId, Long projectId);

	@Query("SELECT COALESCE(SUM(ws.hoursWorked), 0) FROM WorkSession ws "
			+ "WHERE ws.user.id = :userId AND ws.createdAt BETWEEN :startDate AND :endDate")
	BigDecimal getTotalHoursWorkedByUserBetweenDates(@Param("userId") Long userId,
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}