package com.tecsup.app.micro.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentUpdatedEvent {
    private Long enrollmentId;
    private String status;
}
