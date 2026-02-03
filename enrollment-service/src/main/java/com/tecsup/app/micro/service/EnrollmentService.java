package com.tecsup.app.micro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.app.micro.client.CourseClient;
import com.tecsup.app.micro.client.UserClient;
import com.tecsup.app.micro.dto.EnrollmentRequest;
import com.tecsup.app.micro.dto.EnrollmentResponse;
import com.tecsup.app.micro.event.EnrollmentCreatedEvent;
import com.tecsup.app.micro.event.EnrollmentUpdatedEvent;
import com.tecsup.app.micro.event.PaymentApprovedEvent;
import com.tecsup.app.micro.event.PaymentRejectedEvent;
import com.tecsup.app.micro.model.Enrollment;
import com.tecsup.app.micro.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserClient userClient;
    private final CourseClient courseClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_ENROLLMENT_EVENTS = "lms.enrollment.events";

    @Transactional
    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        // Validate User
        if (!userClient.validateUser(request.getUserId())) {
            throw new RuntimeException("User not found or unavailable");
        }

        // Validate Course
        if (!courseClient.validateCourse(request.getCourseId())) {
            throw new RuntimeException("Course not found or unavailable");
        }

        Enrollment enrollment = Enrollment.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .status("PENDING_PAYMENT")
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Publish Event
        EnrollmentCreatedEvent event = EnrollmentCreatedEvent.builder()
                .enrollmentId(saved.getId())
                .userId(saved.getUserId())
                .courseId(saved.getCourseId())
                .status(saved.getStatus())
                .build();
        
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_ENROLLMENT_EVENTS, payload);
            log.info("Published EnrollmentCreatedEvent: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("Error publishing event", e);
        }

        return mapToResponse(saved);
    }

    public EnrollmentResponse getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        return mapToResponse(enrollment);
    }

    public List<EnrollmentResponse> getEnrollmentsByUserId(Long userId) {
        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @KafkaListener(topics = "lms.payment.events", groupId = "enrollment-group")
    public void handlePaymentEvent(String message) {
        log.info("Received payment event: {}", message);
        try {
            // Try determine event type. Since topic is shared, we might need a wrapper or try/catch.
            // Assumption: we can distinguish by content payload or headers.
            // For simplicity, we try to parse as Approved first, then Rejected.
            // A better way is to use headers or separate topics, but requirement says "lms.payment.events".
            
            if (message.contains("PaymentApprovedEvent") || message.contains("\"amount\"")) { 
                // Weak check, but practical for this exercise given simple JSON structure
                 PaymentApprovedEvent event = objectMapper.readValue(message, PaymentApprovedEvent.class);
                 updateEnrollmentStatus(event.getEnrollmentId(), "CONFIRMED");
            } else {
                 PaymentRejectedEvent event = objectMapper.readValue(message, PaymentRejectedEvent.class);
                 updateEnrollmentStatus(event.getEnrollmentId(), "CANCELLED");
            }

        } catch (Exception e) {
            log.error("Error processing payment event", e);
        }
    }

    @Transactional
    public void updateEnrollmentStatus(Long enrollmentId, String status) {
        enrollmentRepository.findById(enrollmentId).ifPresent(enrollment -> {
            enrollment.setStatus(status);
            enrollmentRepository.save(enrollment);
            log.info("Updated enrollment {} status to {}", enrollmentId, status);

            // Publish Updated Event
            EnrollmentUpdatedEvent event = new EnrollmentUpdatedEvent(enrollmentId, status);
            try {
                String payload = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(TOPIC_ENROLLMENT_EVENTS, payload);
            } catch (JsonProcessingException e) {
                 log.error("Error publishing EnrollmentUpdatedEvent", e);
            }
        });
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .status(enrollment.getStatus())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}
