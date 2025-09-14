package com.growcorehub.service;

import com.growcorehub.dto.request.ProfileUpdateRequest;
import com.growcorehub.dto.response.UserResponse;
import com.growcorehub.entity.User;
import com.growcorehub.entity.UserProfile;
import com.growcorehub.enums.VerificationStatus;
import com.growcorehub.exception.BadRequestException;
import com.growcorehub.exception.ResourceNotFoundException;
import com.growcorehub.repository.UserProfileRepository;
import com.growcorehub.repository.UserRepository;
import com.growcorehub.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final ValidationUtil validationUtil;
	private final ApplicationEventPublisher eventPublisher; // Use event publisher for notifications

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

		if (!user.getIsActive()) {
			throw new UsernameNotFoundException("User account is deactivated: " + email);
		}

		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
				user.getEmailVerified(), // enabled
				true, // accountNonExpired
				true, // credentialsNonExpired
				user.getIsActive(), // accountNonLocked
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
	}

	/**
	 * Get user profile information
	 */
	@Transactional(readOnly = true)
	public UserResponse getUserProfile(String email) {
		User user = findByEmail(email);
		return convertToUserResponse(user);
	}

	/**
	 * Update user profile information
	 */
	public UserResponse updateProfile(String email, ProfileUpdateRequest request) {
		User user = findByEmail(email);

		// Validate input data
		validateProfileUpdateRequest(request);

		// Update user basic information
		updateUserBasicInfo(user, request);

		// Update or create user profile
		UserProfile profile = updateUserProfile(user, request);

		// Check if profile is now complete
		checkAndUpdateProfileCompletion(profile);

		// Save changes
		userRepository.save(user);
		userProfileRepository.save(profile);

		log.info("User profile updated for email: {}", email);

		return convertToUserResponse(user);
	}

	/**
	 * Find user by email
	 */
	@Transactional(readOnly = true)
	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
	}

	/**
	 * Find user by ID
	 */
	@Transactional(readOnly = true)
	public User findById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	/**
	 * Check if user exists by email
	 */
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	/**
	 * Verify user email
	 */
	public void verifyUserEmail(String email) {
		User user = findByEmail(email);
		user.setEmailVerified(true);
		userRepository.save(user);

		// Publish event for notification creation (to avoid circular dependency)
		eventPublisher.publishEvent(new NotificationEvent(user, "Email Verified", 
			"Your email has been successfully verified.", 
			com.growcorehub.enums.NotificationType.SYSTEM));

		log.info("Email verified for user: {}", email);
	}

	/**
	 * Update user verification status
	 */
	public void updateVerificationStatus(String email, VerificationStatus status) {
		User user = findByEmail(email);
		UserProfile profile = getUserProfile(user);

		profile.setVerificationStatus(status);
		userProfileRepository.save(profile);

		// Create notification based on status
		String message = switch (status) {
		case VERIFIED -> "Your profile has been verified successfully.";
		case REJECTED -> "Your profile verification was rejected. Please update your information.";
		default -> "Your profile verification is pending review.";
		};

		eventPublisher.publishEvent(new NotificationEvent(user, "Profile Verification Update", message,
				com.growcorehub.enums.NotificationType.SYSTEM));

		log.info("Verification status updated to {} for user: {}", status, email);
	}

	/**
	 * Deactivate user account
	 */
	public void deactivateUser(String email) {
		User user = findByEmail(email);
		user.setIsActive(false);
		userRepository.save(user);
		log.info("User account deactivated: {}", email);
	}

	/**
	 * Activate user account
	 */
	public void activateUser(String email) {
		User user = findByEmail(email);
		user.setIsActive(true);
		userRepository.save(user);
		log.info("User account activated: {}", email);
	}

	/**
	 * Check if user profile is complete
	 */
	@Transactional(readOnly = true)
	public boolean isProfileComplete(String email) {
		User user = findByEmail(email);
		UserProfile profile = getUserProfile(user);
		return profile != null && profile.getProfileCompleted();
	}

	/**
	 * Get user statistics
	 */
	@Transactional(readOnly = true)
	public UserStats getUserStats(String email) {
		User user = findByEmail(email);

		// This would typically involve queries to get application counts, etc.
		// For now, returning basic stats
		return new UserStats(user.getCreatedAt(), user.getEmailVerified(), isProfileComplete(email));
	}

	// Private helper methods

	private void validateProfileUpdateRequest(ProfileUpdateRequest request) {
		if (request.getPhone() != null && !validationUtil.isValidPhone(request.getPhone())) {
			throw new BadRequestException("Invalid phone number format");
		}

		if (request.getAadhaarNumber() != null && !validationUtil.isValidAadhaar(request.getAadhaarNumber())) {
			throw new BadRequestException("Invalid Aadhaar number format");
		}

		if (request.getEducation() != null && !validationUtil.isValidEducation(request.getEducation())) {
			throw new BadRequestException("Education field must be between 2 and 500 characters");
		}

		if (request.getSkills() != null && !validationUtil.isValidSkills(request.getSkills())) {
			throw new BadRequestException("Invalid skills format");
		}

		if (request.getExperienceYears() != null
				&& !validationUtil.isValidExperienceYears(request.getExperienceYears())) {
			throw new BadRequestException("Experience years must be between 0 and 50");
		}
	}

	private void updateUserBasicInfo(User user, ProfileUpdateRequest request) {
		if (request.getPhone() != null) {
			user.setPhone(validationUtil.normalizePhone(request.getPhone()));
		}
	}

	private UserProfile updateUserProfile(User user, ProfileUpdateRequest request) {
		UserProfile profile = getUserProfile(user);

		if (request.getAadhaarNumber() != null) {
			profile.setAadhaarNumber(validationUtil.normalizeAadhaar(request.getAadhaarNumber()));
		}

		if (request.getEducation() != null) {
			profile.setEducation(validationUtil.sanitizeInput(request.getEducation()));
		}

		if (request.getSkills() != null) {
			profile.setSkills(validationUtil.sanitizeInput(request.getSkills()));
		}

		if (request.getExperienceYears() != null) {
			profile.setExperienceYears(request.getExperienceYears());
		}

		return profile;
	}

	private UserProfile getUserProfile(User user) {
		return userProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
			UserProfile newProfile = new UserProfile();
			newProfile.setUser(user);
			newProfile.setVerificationStatus(VerificationStatus.PENDING);
			newProfile.setProfileCompleted(false);
			return newProfile;
		});
	}

	private void checkAndUpdateProfileCompletion(UserProfile profile) {
		boolean isComplete = profile.getAadhaarNumber() != null && profile.getEducation() != null
				&& profile.getSkills() != null && profile.getExperienceYears() != null;

		if (isComplete && !profile.getProfileCompleted()) {
			profile.setProfileCompleted(true);
			log.info("Profile marked as completed for user: {}", profile.getUser().getEmail());
		} else if (!isComplete && profile.getProfileCompleted()) {
			profile.setProfileCompleted(false);
			log.info("Profile marked as incomplete for user: {}", profile.getUser().getEmail());
		}
	}

	private UserResponse convertToUserResponse(User user) {
		UserResponse response = new UserResponse();

		// Basic user information
		response.setId(user.getId());
		response.setEmail(user.getEmail());
		response.setFirstName(user.getFirstName());
		response.setLastName(user.getLastName());
		response.setPhone(user.getPhone());
		response.setEmailVerified(user.getEmailVerified());
		response.setCreatedAt(user.getCreatedAt());

		// Profile information
		UserProfile profile = getUserProfile(user);
		if (profile != null) {
			response.setAadhaarNumber(profile.getAadhaarNumber());
			response.setEducation(profile.getEducation());
			response.setSkills(profile.getSkills());
			response.setExperienceYears(profile.getExperienceYears());
			response.setProfileCompleted(profile.getProfileCompleted());
			response.setVerificationStatus(profile.getVerificationStatus());
		}

		return response;
	}

	// Inner class for user statistics
	public static class UserStats {
		private final java.time.LocalDateTime joinedAt;
		private final Boolean emailVerified;
		private final Boolean profileComplete;

		public UserStats(java.time.LocalDateTime joinedAt, Boolean emailVerified, Boolean profileComplete) {
			this.joinedAt = joinedAt;
			this.emailVerified = emailVerified;
			this.profileComplete = profileComplete;
		}

		// Getters
		public java.time.LocalDateTime getJoinedAt() {
			return joinedAt;
		}

		public Boolean getEmailVerified() {
			return emailVerified;
		}

		public Boolean getProfileComplete() {
			return profileComplete;
		}
	}

	// Event class for notifications
	public static class NotificationEvent {
		private final User user;
		private final String title;
		private final String message;
		private final com.growcorehub.enums.NotificationType type;

		public NotificationEvent(User user, String title, String message, com.growcorehub.enums.NotificationType type) {
			this.user = user;
			this.title = title;
			this.message = message;
			this.type = type;
		}

		public User getUser() { return user; }
		public String getTitle() { return title; }
		public String getMessage() { return message; }
		public com.growcorehub.enums.NotificationType getType() { return type; }
	}
}