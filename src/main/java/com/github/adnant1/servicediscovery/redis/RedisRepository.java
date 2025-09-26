package com.github.adnant1.servicediscovery.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Repository class for managing service registry data in Redis.
 */
public class RedisRepository {
    private final StringRedisTemplate redisTemplate;
    private final int ttlSeconds;

    public RedisRepository(StringRedisTemplate redisTemplate, 
                          @Value("${spring.ttl-seconds:30}") int ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }
}
