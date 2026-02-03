package com.tecsup.app.micro.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic courseEventsTopic() {
        return TopicBuilder.name("lms.course.events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
