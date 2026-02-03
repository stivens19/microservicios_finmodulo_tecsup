package com.tecsup.app.micro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long enrollmentId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paidAt;
}
