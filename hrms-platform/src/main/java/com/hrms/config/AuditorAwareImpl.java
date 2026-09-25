package com.hrms.config;

import com.hrms.common.util.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            return Optional.of(SecurityUtils.getCurrentUsername());
        } catch (Exception e) {
            return Optional.of("system");
        }
    }
}
