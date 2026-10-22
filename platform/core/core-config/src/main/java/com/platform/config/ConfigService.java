package com.platform.config;

import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final PlatformConfigRepository configRepository;

    @Cacheable(value = "configs", key = "#key + ':' + T(com.platform.tenancy.TenantContext).getTenantId()")
    public String get(String key, String defaultValue) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return configRepository.findByKeyAndCompanyId(key, companyId)
            .map(PlatformConfig::getValue)
            .orElse(defaultValue);
    }

    public String get(String key) {
        return get(key, null);
    }

    public int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null) return defaultValue;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultValue; }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    @Transactional
    @CacheEvict(value = "configs", key = "#key + ':' + T(com.platform.tenancy.TenantContext).getTenantId()")
    public void set(String key, String value, String category) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        PlatformConfig config = configRepository.findByKeyAndCompanyId(key, companyId)
            .orElse(PlatformConfig.builder().key(key).category(category).build());
        config.setValue(value);
        config.setCompanyId(companyId);
        configRepository.save(config);
    }
}
