package com.growcorehub.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

	// Email validation pattern
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

	// Phone validation pattern (supports international formats)
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[1-9]\\d{1,14}$");

	// Indian Aadhaar number pattern (with spaces)
	private static final Pattern AADHAAR_PATTERN = Pattern.compile("^[2-9]{1}[0-9]{3}\\s[0-9]{4}\\s[0-9]{4}$");

	// Indian Aadhaar number pattern (without spaces)
	private static final Pattern AADHAAR_PATTERN_NO_SPACE = Pattern.compile("^[2-9]{1}[0-9]{11}$");

	// Password strength pattern (min 8 chars, at least 1 uppercase, 1 lowercase, 1
	// number)
	private static final Pattern PASSWORD_PATTERN = Pattern
			.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");

	// Name validation pattern (only letters, spaces, and common name characters)
	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]{2,50}$");

	/**
	 * Validates email format
	 */
	public boolean isValidEmail(String email) {
		return email != null && email.length() <= 255 && EMAIL_PATTERN.matcher(email.trim()).matches();
	}

	/**
	 * Validates phone number format
	 */
	public boolean isValidPhone(String phone) {
		if (phone == null)
			return false;

		String cleanPhone = phone.replaceAll("[\\s()-]", "");
		return PHONE_PATTERN.matcher(cleanPhone).matches();
	}

	/**
	 * Validates Indian Aadhaar number format
	 */
	public boolean isValidAadhaar(String aadhaar) {
		if (aadhaar == null)
			return false;

		return AADHAAR_PATTERN.matcher(aadhaar).matches() || AADHAAR_PATTERN_NO_SPACE.matcher(aadhaar).matches();
	}

	/**
	 * Validates password strength
	 */
	public boolean isValidPassword(String password) {
		return password != null && password.length() >= 8 && password.length() <= 100
				&& PASSWORD_PATTERN.matcher(password).matches();
	}

	/**
	 * Validates name format
	 */
	public boolean isValidName(String name) {
		return name != null && NAME_PATTERN.matcher(name.trim()).matches();
	}

	/**
	 * Normalizes phone number by removing formatting characters
	 */
	public String normalizePhone(String phone) {
		if (phone == null)
			return null;
		return phone.replaceAll("[\\s()-]", "");
	}

	/**
	 * Normalizes Aadhaar number by adding standard formatting
	 */
	public String normalizeAadhaar(String aadhaar) {
		if (aadhaar == null)
			return null;

		String cleanAadhaar = aadhaar.replaceAll("\\s", "");
		if (cleanAadhaar.length() == 12) {
			return cleanAadhaar.substring(0, 4) + " " + cleanAadhaar.substring(4, 8) + " "
					+ cleanAadhaar.substring(8, 12);
		}
		return aadhaar;
	}

	/**
	 * Validates skill string format
	 */
	public boolean isValidSkills(String skills) {
		if (skills == null || skills.trim().isEmpty())
			return false;

		// Check if skills contain only valid characters and aren't too long
		return skills.length() <= 1000 && skills.matches("^[a-zA-Z0-9\\s,.-]+$");
	}

	/**
	 * Validates experience years
	 */
	public boolean isValidExperienceYears(Integer years) {
		return years != null && years >= 0 && years <= 50;
	}

	/**
	 * Validates education string
	 */
	public boolean isValidEducation(String education) {
		return education != null && education.trim().length() >= 2 && education.length() <= 500;
	}

	/**
	 * Sanitizes input by trimming and removing potentially harmful characters
	 */
	public String sanitizeInput(String input) {
		if (input == null)
			return null;

		return input.trim().replaceAll("[<>\"'&]", "") // Remove potential XSS characters
				.replaceAll("\\s+", " "); // Normalize whitespace
	}

	/**
	 * Validates if a string is not null, not empty, and within length limits
	 */
	public boolean isValidString(String str, int minLength, int maxLength) {
		return str != null && str.trim().length() >= minLength && str.trim().length() <= maxLength;
	}
}