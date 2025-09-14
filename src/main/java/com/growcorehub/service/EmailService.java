package com.growcorehub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;

	public void sendSimpleMessage(String to, String subject, String text) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("noreply@growcorehub.com");
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			mailSender.send(message);
			log.info("Email sent successfully to: {}", to);
		} catch (Exception e) {
			log.error("Failed to send email to: {}, error: {}", to, e.getMessage());
		}
	}

	public void sendWelcomeEmail(String email, String firstName) {
		String subject = "Welcome to Grow Core Hub!";
		String text = String
				.format("Dear %s,\n\n" + "Welcome to Grow Core Hub! We're excited to have you join our platform.\n\n"
						+ "Please complete your profile to start applying for projects.\n\n" + "Best regards,\n"
						+ "Grow Core Hub Team", firstName);
		sendSimpleMessage(email, subject, text);
	}

	public void sendApplicationNotification(String email, String projectTitle) {
		String subject = "Application Submitted Successfully";
		String text = String.format("Your application for the project '%s' has been submitted successfully.\n\n"
				+ "We will notify you once your application is reviewed.\n\n" + "Best regards,\n"
				+ "Grow Core Hub Team", projectTitle);
		sendSimpleMessage(email, subject, text);
	}

	public void sendPasswordResetEmail(String to, String token) throws Exception {
		String subject = "Password Reset Request";
		String resetUrl = "http://localhost:8080/reset-password?token=" + token;

		String body = "<p>Hello,</p>" + "<p>You requested to reset your password.</p>"
				+ "<p>Click the link below to reset it:</p>" + "<p><a href=\"" + resetUrl + "\">Reset Password</a></p>"
				+ "<br>" + "<p>If you did not request a password reset, please ignore this email.</p>";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, true); // true = HTML

		mailSender.send(message);
	}

}