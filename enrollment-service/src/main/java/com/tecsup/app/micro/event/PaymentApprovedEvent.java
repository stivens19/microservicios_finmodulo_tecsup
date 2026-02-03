package com.tecsup.app.micro.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApprovedEvent {
    private Long paymentId;
    private Long enrollmentId;
    private double amount;
}
