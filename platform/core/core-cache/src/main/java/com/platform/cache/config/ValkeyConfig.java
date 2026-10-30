package com.platform.cache.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class ValkeyConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    // ─────────────────────────────────────────────
    // Connection Factory → connects to Valkey server
    // ─────────────────────────────────────────────
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        return factory;
    }

    // ─────────────────────────────────────────────
    // Listener Container → required for pub/sub
    // ─────────────────────────────────────────────
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        return container;
    }

    // ─────────────────────────────────────────────
    // RedisTemplate → general purpose Valkey operations
    // Used for sessions, pub/sub, custom cache
    // ─────────────────────────────────────────────
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // ─────────────────────────────────────────────
    // Cache Manager → Spring @Cacheable annotation
    // Different TTL per module cache
    // ─────────────────────────────────────────────
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // HRMS caches
        cacheConfigs.put("employees",    ttl(30));   // 30 minutes
        cacheConfigs.put("departments",  ttl(120));  // 2 hours
        cacheConfigs.put("designations", ttl(120));  // 2 hours
        cacheConfigs.put("branches",     ttl(120));  // 2 hours
        cacheConfigs.put("teams",        ttl(60));   // 1 hour

        // Payroll caches
        cacheConfigs.put("payroll",      ttl(60));   // 1 hour
        cacheConfigs.put("payslips",     ttl(60));   // 1 hour

        // Ticket caches
        cacheConfigs.put("tickets",      ttl(15));   // 15 minutes
        cacheConfigs.put("projects",     ttl(30));   // 30 minutes

        // Support caches
        cacheConfigs.put("support",      ttl(15));   // 15 minutes

        // Finance caches
        cacheConfigs.put("finance",      ttl(30));   // 30 minutes

        // Auth caches
        cacheConfigs.put("users",        ttl(30));   // 30 minutes
        cacheConfigs.put("roles",        ttl(120));  // 2 hours
        cacheConfigs.put("permissions",  ttl(120));  // 2 hours

        return RedisCacheManager.builder(factory)
                .cacheDefaults(ttl(30))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    private RedisCacheConfiguration ttl(long minutes) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(minutes))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                );
    }
}
