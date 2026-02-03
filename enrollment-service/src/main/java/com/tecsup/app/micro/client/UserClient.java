package com.tecsup.app.micro.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${service.user.url}")
    private String userServiceUrl;

    public boolean validateUser(Long userId) {
        try {
            restTemplate.getForEntity(userServiceUrl + "/users/" + userId, Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
