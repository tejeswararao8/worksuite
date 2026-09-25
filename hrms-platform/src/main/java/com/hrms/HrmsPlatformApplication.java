package com.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableCaching
@EnableScheduling
public class HrmsPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsPlatformApplication.class, args);
    }
}
