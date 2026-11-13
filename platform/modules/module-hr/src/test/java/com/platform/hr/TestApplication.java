package com.platform.hr;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Minimal Spring Boot configuration for WebMvcTest slices in module-hr.
 * Placed at the root package so @SpringBootApplication scans all subpackages.
 * Excludes database auto-configuration — tests use mocked services only.
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class,
})
public class TestApplication {
}
