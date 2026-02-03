package com.tecsup.app.micro.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {
    private Long id;
    private Long userId;
    private Long courseId;
    private String status;
    private LocalDateTime createdAt;
}
