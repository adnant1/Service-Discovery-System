package com.github.adnant1.servicediscovery.redis;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Repository class for managing service registry data in Redis.
 */
public class RedisRepository {
 
    private final StringRedisTemplate redisTemplate;

    public RedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
