package com.growcorehub.service;

import com.growcorehub.entity.Notification;
import com.growcorehub.entity.User;
import com.growcorehub.enums.NotificationType;
import com.growcorehub.repository.NotificationRepository;
import com.growcorehub.repository.UserRepository;
import com.growcorehub.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository; // Use UserRepository directly instead of UserService

	public Page<Notification> getUserNotifications(String userEmail, Pageable pageable) {
		User user = findUserByEmail(userEmail);
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
	}

	public long getUnreadNotificationCount(String userEmail) {
		User user = findUserByEmail(userEmail);
		return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
	}

	public void markAsRead(Long notificationId, String userEmail) {
		User user = findUserByEmail(userEmail);
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new RuntimeException("Notification not found"));

		if (!notification.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("Unauthorized access to notification");
		}

		notification.setIsRead(true);
		notificationRepository.save(notification);
	}

	public void createNotification(User user, String title, String message, NotificationType type) {
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setType(type);
		notificationRepository.save(notification);
	}

	// Helper method to find user by email without depending on UserService
	private User findUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
	}
}