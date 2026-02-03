package com.tecsup.app.micro.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.app.micro.event.*;
import com.tecsup.app.micro.model.Notification;
import com.tecsup.app.micro.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "lms.enrollment.events", groupId = "notification-group")
    public void handleEnrollmentEvent(String message) {
        log.info("Servicio de notificación de evento de inscripción recibido: {}", message);
        try {
            if (message.contains("EnrollmentCreatedEvent") || message.contains("userId")) {
                EnrollmentCreatedEvent event = objectMapper.readValue(message, EnrollmentCreatedEvent.class);
                saveNotification(event.getUserId(), "Inscripción creada para curso " + event.getCourseId() + ". Estado: " + event.getStatus());
            } else {
                EnrollmentUpdatedEvent event = objectMapper.readValue(message, EnrollmentUpdatedEvent.class);
                // We need userId for notification, but UpdatedEvent might not have it.
                // In a real system we would fetch it or include it in event.
                // For now, we log it or save with null userId (broadcast/admin notification)
                saveNotification(null, "Inscripción " + event.getEnrollmentId() + " actualizada a estado: " + event.getStatus());
            }
        } catch (Exception e) {
            log.error("Error procesando evento de inscripción", e);
        }
    }

    @KafkaListener(topics = "lms.payment.events", groupId = "notification-group")
    public void handlePaymentEvent(String message) {
        log.info("Servicio de notificación de evento de pago recibido: {}", message);
        try {
             if (message.contains("PaymentApprovedEvent") || message.contains("\"amount\"")) {
                 PaymentApprovedEvent event = objectMapper.readValue(message, PaymentApprovedEvent.class);
                 saveNotification(null, "Pago " + event.getPaymentId() + " aprobado para inscripción " + event.getEnrollmentId());
             } else {
                 PaymentRejectedEvent event = objectMapper.readValue(message, PaymentRejectedEvent.class);
                 saveNotification(null, "Pago " + event.getPaymentId() + " rechazado para inscripción " + event.getEnrollmentId());
             }
        } catch (Exception e) {
            log.error("Error procesando evento de pago", e);
        }
    }

    @KafkaListener(topics = "lms.course.events", groupId = "notification-group")
    public void handleCourseEvent(String message) {
        log.info("Servicio de notificación de evento de curso recibido: {}", message);
        try {
            CoursePublishedEvent event = objectMapper.readValue(message, CoursePublishedEvent.class);
            saveNotification(null, "Nuevo curso publicado: " + event.getTitle());
        } catch (Exception e) {
            log.error("Error procesando evento de curso", e);
        }
    }

    private void saveNotification(Long userId, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .sent(true) // Simulate sending
                .build();
        notificationRepository.save(notification);
        log.info("Notificación guardada: {}", message);
    }
}
