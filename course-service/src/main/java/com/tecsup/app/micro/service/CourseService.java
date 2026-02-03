package com.tecsup.app.micro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.app.micro.dto.CourseRequest;
import com.tecsup.app.micro.dto.CourseResponse;
import com.tecsup.app.micro.event.CoursePublishedEvent;
import com.tecsup.app.micro.model.Course;
import com.tecsup.app.micro.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "lms.course.events";

    public CourseResponse createCourse(CourseRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .published(false)
                .build();
        Course saved = courseRepository.save(course);
        return mapToResponse(saved);
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no disponible"));
        return mapToResponse(course);
    }

    @Transactional
    public void publishCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no disponible"));
        
        course.setPublished(true);
        courseRepository.save(course);

        // Send event
        CoursePublishedEvent event = new CoursePublishedEvent(course.getId(), course.getTitle());
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, payload);
            log.info("Publicado evento: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("Error serializando evento", e);
        }
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .published(course.isPublished())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
