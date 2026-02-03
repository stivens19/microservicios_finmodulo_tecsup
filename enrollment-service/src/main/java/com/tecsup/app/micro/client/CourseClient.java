package com.tecsup.app.micro.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class CourseClient {

    private final RestTemplate restTemplate;

    @Value("${service.course.url}")
    private String courseServiceUrl;

    public boolean validateCourse(Long courseId) {
        try {
            restTemplate.getForEntity(courseServiceUrl + "/courses/" + courseId, Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
