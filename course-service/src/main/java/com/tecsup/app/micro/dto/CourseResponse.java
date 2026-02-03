package com.tecsup.app.micro.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private boolean published;
    private LocalDateTime createdAt;
}
