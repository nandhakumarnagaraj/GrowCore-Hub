package com.growcorehub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationService notificationService;

	@EventListener
	@Async
	public void handleNotificationEvent(UserService.NotificationEvent event) {
		notificationService.createNotification(event.getUser(), event.getTitle(), event.getMessage(), event.getType());
	}
}