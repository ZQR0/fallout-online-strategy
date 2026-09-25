package com.fallout.lobby.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    /**
     * Configures a ReactiveStringRedisTemplate for non-blocking operations
     * with String keys and values. This is the primary tool for managing
     * session state and metadata in Redis.
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        log.info("Configuring ReactiveStringRedisTemplate for lobby session management");
        return new ReactiveStringRedisTemplate(factory);
    }
}
