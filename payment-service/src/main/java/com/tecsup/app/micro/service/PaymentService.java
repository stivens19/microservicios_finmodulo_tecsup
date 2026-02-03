package com.tecsup.app.micro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.app.micro.dto.PaymentRequest;
import com.tecsup.app.micro.dto.PaymentResponse;
import com.tecsup.app.micro.event.PaymentApprovedEvent;
import com.tecsup.app.micro.event.PaymentRejectedEvent;
import com.tecsup.app.micro.model.Payment;
import com.tecsup.app.micro.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_PAYMENT_EVENTS = "lms.payment.events";

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Business Logic: 
        // For simplicity, payments > 0 are approved. 0 or less rejected (handled by @Positive validation usually, but lets simulate)
        // Or reject if amount > 10000 just for fun. No, stick to simple.
        // Always Approve for now.
        
        Payment payment = Payment.builder()
                .enrollmentId(request.getEnrollmentId())
                .amount(request.getAmount())
                .status("APPROVED")
                .build();

        Payment saved = paymentRepository.save(payment);

        // Publish Event
        try {
            if ("APPROVED".equals(saved.getStatus())) {
                PaymentApprovedEvent event = PaymentApprovedEvent.builder()
                        .paymentId(saved.getId())
                        .enrollmentId(saved.getEnrollmentId())
                        .amount(saved.getAmount())
                        .build();
                String payload = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(TOPIC_PAYMENT_EVENTS, payload);
                log.info("Publicado PaymentApprovedEvent: {}", payload);
            } else {
                PaymentRejectedEvent event = PaymentRejectedEvent.builder()
                        .paymentId(saved.getId())
                        .enrollmentId(saved.getEnrollmentId())
                        .build();
                String payload = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(TOPIC_PAYMENT_EVENTS, payload);
                log.info("Publicado PaymentRejectedEvent: {}", payload);
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializando evento de pago", e);
        }

        return mapToResponse(saved);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .enrollmentId(payment.getEnrollmentId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
