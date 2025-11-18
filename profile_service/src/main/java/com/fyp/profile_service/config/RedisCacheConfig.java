package com.fyp.profile_service.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    private ObjectMapper createObjectMapperWithTyping() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return objectMapper;
    }

    private ObjectMapper createObjectMapperWithoutTyping() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }

    @Bean
    public RedisCacheManager cacheConfiguration(RedisConnectionFactory redisConnectionFactory) {
        // Default configuration with typing (for single objects)
        GenericJackson2JsonRedisSerializer defaultSerializer =
                new GenericJackson2JsonRedisSerializer(createObjectMapperWithTyping());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(defaultSerializer));

        GenericJackson2JsonRedisSerializer listSerializer =
                new GenericJackson2JsonRedisSerializer(createObjectMapperWithoutTyping());

        RedisCacheConfiguration listConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(listSerializer));

        // Map specific caches to their configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User profile caching - low mutation, high read frequency
        cacheConfigurations.put("PROFILE_CACHE", defaultConfig);

        // Doctor profile caching - low mutation, very high read frequency (appointment creation)
        cacheConfigurations.put("DOCTOR_PROFILE_CACHE", defaultConfig);

        // Note: Prescription caching removed due to:
        // - High mutation frequency (status changes)
        // - Dynamic access control (per-doctor permissions)
        // - Complex cross-user modifications
        // Direct database reads provide better consistency

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
