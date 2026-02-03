package com.tecsup.app.micro.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentCreatedEvent {
    private Long enrollmentId;
    private Long userId;
    private Long courseId;
    private String status;
}
