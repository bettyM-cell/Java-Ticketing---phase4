package com.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.ticket.domain")
@EnableJpaRepositories(basePackages = "com.ticket.repository")
public class JavaTicketingApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JavaTicketingApplication.class);

        // Using .put() ensures perfect compilation compliance
        Map<String, Object> props = new HashMap<>();
        props.put("spring.profiles.active", "test");
        props.put("spring.main.web-application-type", "servlet");
        app.setDefaultProperties(props);

        app.run(args);
    }
}

