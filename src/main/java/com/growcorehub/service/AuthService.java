package com.growcorehub.service;

import com.growcorehub.dto.request.LoginRequest;
import com.growcorehub.dto.request.RegisterRequest;
import com.growcorehub.dto.response.AuthResponse;
import com.growcorehub.dto.response.UserResponse;
import com.growcorehub.entity.User;
import com.growcorehub.entity.UserProfile;
import com.growcorehub.enums.NotificationType;
import com.growcorehub.enums.VerificationStatus;
import com.growcorehub.exception.BadRequestException;
import com.growcorehub.repository.UserProfileRepository;
import com.growcorehub.repository.UserRepository;
import com.growcorehub.util.JwtUtil;
import com.growcorehub.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserService userService;
	private final ValidationUtil validationUtil;
	private final EmailService emailService;
	private final NotificationService notificationService;

	/**
	 * Authenticate user and return JWT token
	 */
	public AuthResponse login(LoginRequest request) {
		try {
			// Validate input
			validateLoginRequest(request);

			// Authenticate user
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					request.getEmail().toLowerCase().trim(), request.getPassword()));

			// Generate JWT token
			String jwt = jwtUtil.generateJwtToken(authentication);

			// Get user details
			UserResponse userResponse = userService.getUserProfile(request.getEmail());

			log.info("User logged in successfully: {}", request.getEmail());

			return new AuthResponse(jwt, userResponse);

		} catch (BadCredentialsException e) {
			log.warn("Failed login attempt for email: {}", request.getEmail());
			throw new BadRequestException("Invalid email or password");
		} catch (DisabledException e) {
			log.warn("Login attempt for disabled account: {}", request.getEmail());
			throw new BadRequestException("Account is disabled. Please contact support.");
		} catch (AuthenticationException e) {
			log.error("Authentication error for email: {}: {}", request.getEmail(), e.getMessage());
			throw new BadRequestException("Authentication failed");
		}
	}

	/**
	 * Register new user account
	 */
	public AuthResponse register(RegisterRequest request) {
		// Validate input
		validateRegistrationRequest(request);

		// Check if user already exists
		if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
			throw new BadRequestException("Email is already registered");
		}

		try {
			// Create user
			User user = createUser(request);
			User savedUser = userRepository.save(user);

			// Create user profile
			UserProfile profile = createUserProfile(savedUser);
			userProfileRepository.save(profile);

			// Generate JWT token
			String jwt = jwtUtil.generateTokenFromEmail(savedUser.getEmail());

			// Get user response
			UserResponse userResponse = userService.getUserProfile(savedUser.getEmail());

			// Send welcome email asynchronously
			sendWelcomeEmailAsync(savedUser);

			// Create welcome notification
			notificationService.createNotification(savedUser, "Welcome to Grow Core Hub!",
					"Welcome to our platform! Please complete your profile to start applying for projects.",
					NotificationType.SYSTEM);

			log.info("User registered successfully: {}", savedUser.getEmail());

			return new AuthResponse(jwt, userResponse);

		} catch (Exception e) {
			log.error("Error during user registration: {}", e.getMessage(), e);
			throw new BadRequestException("Registration failed. Please try again.");
		}
	}

	/**
	 * Refresh JWT token
	 */
	public AuthResponse refreshToken(String refreshToken) {
		try {
			if (!jwtUtil.validateJwtToken(refreshToken)) {
				throw new BadRequestException("Invalid refresh token");
			}

			String email = jwtUtil.getEmailFromJwtToken(refreshToken);
			User user = userService.findByEmail(email);

			if (!user.getIsActive()) {
				throw new BadRequestException("Account is deactivated");
			}

			String newToken = jwtUtil.generateTokenFromEmail(email);
			UserResponse userResponse = userService.getUserProfile(email);

			return new AuthResponse(newToken, userResponse);

		} catch (Exception e) {
			log.error("Error refreshing token: {}", e.getMessage());
			throw new BadRequestException("Failed to refresh token");
		}
	}

	/**
	 * Initiate password reset process
	 * @throws Exception 
	 */
	public void initiatePasswordReset(String email) throws Exception {
		User user = userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);

		if (user != null) {
			// Generate reset token (simplified - in production, use a proper reset token)
			String resetToken = jwtUtil.generateTokenFromEmail(email);

			// Send reset email
			emailService.sendPasswordResetEmail(email, resetToken);

			log.info("Password reset initiated for email: {}", email);
		} else {
			log.warn("Password reset requested for non-existent email: {}", email);
		}

		// Always return success for security (don't reveal if email exists)
	}

	/**
	 * Reset password with token
	 */
	public void resetPassword(String token, String newPassword) {
		try {
			if (!jwtUtil.validateJwtToken(token)) {
				throw new BadRequestException("Invalid or expired reset token");
			}

			String email = jwtUtil.getEmailFromJwtToken(token);
			User user = userService.findByEmail(email);

			if (!validationUtil.isValidPassword(newPassword)) {
				throw new BadRequestException(
						"Password must be at least 8 characters long and contain uppercase, lowercase, and numeric characters");
			}

			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);

			// Create notification
			notificationService.createNotification(user, "Password Reset", "Your password has been successfully reset.",
					NotificationType.SYSTEM);

			log.info("Password reset successfully for email: {}", email);

		} catch (Exception e) {
			log.error("Error resetting password: {}", e.getMessage());
			throw new BadRequestException("Failed to reset password");
		}
	}

	/**
	 * Change password for authenticated user
	 */
	public void changePassword(String email, String currentPassword, String newPassword) {
		User user = userService.findByEmail(email);

		// Verify current password
		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new BadRequestException("Current password is incorrect");
		}

		// Validate new password
		if (!validationUtil.isValidPassword(newPassword)) {
			throw new BadRequestException(
					"New password must be at least 8 characters long and contain uppercase, lowercase, and numeric characters");
		}

		// Update password
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		// Create notification
		notificationService.createNotification(user, "Password Changed", "Your password has been successfully changed.",
				NotificationType.SYSTEM);

		log.info("Password changed successfully for email: {}", email);
	}

	// Private helper methods

	private void validateLoginRequest(LoginRequest request) {
		if (!validationUtil.isValidEmail(request.getEmail())) {
			throw new BadRequestException("Invalid email format");
		}

		if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
			throw new BadRequestException("Password is required");
		}
	}

	private void validateRegistrationRequest(RegisterRequest request) {
		if (!validationUtil.isValidEmail(request.getEmail())) {
			throw new BadRequestException("Invalid email format");
		}

		if (!validationUtil.isValidPassword(request.getPassword())) {
			throw new BadRequestException(
					"Password must be at least 8 characters long and contain uppercase, lowercase, and numeric characters");
		}

		if (!validationUtil.isValidName(request.getFirstName())) {
			throw new BadRequestException("First name must be between 2 and 50 characters and contain only letters");
		}

		if (!validationUtil.isValidName(request.getLastName())) {
			throw new BadRequestException("Last name must be between 2 and 50 characters and contain only letters");
		}

		if (request.getPhone() != null && !request.getPhone().trim().isEmpty()
				&& !validationUtil.isValidPhone(request.getPhone())) {
			throw new BadRequestException("Invalid phone number format");
		}
	}

	private User createUser(RegisterRequest request) {
		User user = new User();
		user.setEmail(request.getEmail().toLowerCase().trim());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setFirstName(validationUtil.sanitizeInput(request.getFirstName()).trim());
		user.setLastName(validationUtil.sanitizeInput(request.getLastName()).trim());

		if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
			user.setPhone(validationUtil.normalizePhone(request.getPhone()));
		}

		user.setEmailVerified(false);
		user.setIsActive(true);

		return user;
	}

	private UserProfile createUserProfile(User user) {
		UserProfile profile = new UserProfile();
		profile.setUser(user);
		profile.setVerificationStatus(VerificationStatus.PENDING);
		profile.setProfileCompleted(false);
		profile.setExperienceYears(0);

		return profile;
	}

	private void sendWelcomeEmailAsync(User user) {
		try {
			emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
		} catch (Exception e) {
			log.error("Failed to send welcome email to: {}", user.getEmail(), e);
			// Don't throw exception as this shouldn't fail the registration
		}
	}
}