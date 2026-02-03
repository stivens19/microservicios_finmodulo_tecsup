package com.tecsup.app.micro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull
    private Long enrollmentId;
    
    @NotNull
    @Positive
    private BigDecimal amount;
}
