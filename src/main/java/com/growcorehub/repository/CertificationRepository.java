package com.growcorehub.repository;

import com.growcorehub.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
	
	List<Certification> findByUserId(Long userId);

	List<Certification> findByUserIdOrderByEarnedAtDesc(Long userId);
}