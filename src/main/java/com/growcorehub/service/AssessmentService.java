package com.growcorehub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.growcorehub.dto.request.AssessmentSubmissionRequest;
import com.growcorehub.dto.response.AssessmentResponse;
import com.growcorehub.entity.Assessment;
import com.growcorehub.entity.Certification;
import com.growcorehub.entity.User;
import com.growcorehub.entity.UserAssessment;
import com.growcorehub.exception.BadRequestException;
import com.growcorehub.exception.ResourceNotFoundException;
import com.growcorehub.repository.AssessmentRepository;
import com.growcorehub.repository.CertificationRepository;
import com.growcorehub.repository.UserAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final UserAssessmentRepository userAssessmentRepository;
    private final CertificationRepository certificationRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public List<AssessmentResponse> getAssessmentsByProjectId(Long projectId, Long userId) {
        List<Assessment> assessments = assessmentRepository.findByProjectId(projectId);

        return assessments.stream()
            .map(assessment -> convertToAssessmentResponse(assessment, userId))
            .collect(Collectors.toList());
    }

    public AssessmentResponse getAssessmentById(Long id, String userEmail) {
        Assessment assessment = assessmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));

        User user = userService.findByEmail(userEmail);
        return convertToAssessmentResponse(assessment, user.getId());
    }

    public AssessmentResponse submitAssessment(AssessmentSubmissionRequest request, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Assessment assessment = assessmentRepository.findById(request.getAssessmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        // Check if user has already completed this assessment
        if (userAssessmentRepository.existsByUserIdAndAssessmentId(user.getId(), assessment.getId())) {
            throw new BadRequestException("Assessment already completed");
        }

        // Calculate score (simplified scoring logic)
        BigDecimal score = calculateScore(assessment, request.getAnswers());

        // Save user assessment
        UserAssessment userAssessment = new UserAssessment();
        userAssessment.setUser(user);
        userAssessment.setAssessment(assessment);
        userAssessment.setScore(score);
        userAssessment.setAnswers(request.getAnswers());
        userAssessmentRepository.save(userAssessment);

        // Create certification if score meets criteria
        if (score.compareTo(new BigDecimal("70")) >= 0) {
            createCertification(user, assessment, score);
        }

        return convertToAssessmentResponse(assessment, user.getId());
    }

    private BigDecimal calculateScore(Assessment assessment, String answersJson) {
        try {
            // Parse questions and answers
            List<Map<String, Object>> questions = objectMapper.readValue(
                assessment.getQuestions(), new TypeReference<List<Map<String, Object>>>() {}
            );
            Map<String, Object> answers = objectMapper.readValue(
                answersJson, new TypeReference<Map<String, Object>>() {}
            );

            int correctAnswers = 0;
            for (int i = 0; i < questions.size(); i++) {
                Map<String, Object> question = questions.get(i);
                String correctAnswer = (String) question.get("correctAnswer");
                String userAnswer = (String) answers.get("question_" + i);

                if (correctAnswer != null && correctAnswer.equals(userAnswer)) {
                    correctAnswers++;
                }
            }

            return new BigDecimal((correctAnswers * 100.0) / questions.size());
        } catch (Exception e) {
            log.error("Error calculating score for assessment {}: {}", assessment.getId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private void createCertification(User user, Assessment assessment, BigDecimal score) {
        // Extract skill name from assessment name or project
        String skillName = assessment.getName();

        Certification certification = new Certification();
        certification.setUser(user);
        certification.setSkillName(skillName);
        certification.setScore(score);
        certification.setAssessment(assessment);
        certificationRepository.save(certification);
    }

    private AssessmentResponse convertToAssessmentResponse(Assessment assessment, Long userId) {
        AssessmentResponse response = new AssessmentResponse();
        response.setId(assessment.getId());
        response.setName(assessment.getName());
        response.setDescription(assessment.getDescription());
        response.setMaxScore(assessment.getMaxScore());
        response.setTimeLimitMinutes(assessment.getTimeLimitMinutes());

        // Parse questions (hide correct answers for security)
        try {
            if (assessment.getQuestions() != null) {
                List<Map<String, Object>> questions = objectMapper.readValue(
                    assessment.getQuestions(), new TypeReference<List<Map<String, Object>>>() {}
                );
                
                // Remove correct answers from response
                questions.forEach(q -> q.remove("correctAnswer"));
                
                // Convert to QuestionResponse format would go here
                // For simplicity, we'll set questions as null for now
                response.setQuestions(null);
            }
        } catch (Exception e) {
            log.error("Error parsing questions for assessment {}: {}", assessment.getId(), e.getMessage());
        }

        // Check if user has completed this assessment
        if (userId != null) {
            UserAssessment userAssessment = userAssessmentRepository
                .findByUserIdAndAssessmentId(userId, assessment.getId())
                .orElse(null);
            
            if (userAssessment != null) {
                response.setIsCompleted(true);
                response.setUserScore(userAssessment.getScore());
                response.setCompletedAt(userAssessment.getCompletedAt());
            } else {
                response.setIsCompleted(false);
            }
        }

        return response;
    }
}