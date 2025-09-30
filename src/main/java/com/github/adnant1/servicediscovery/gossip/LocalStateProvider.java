package com.github.adnant1.servicediscovery.gossip;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LocalStateProvider {
 
    private final StringRedisTemplate redisTemplate;

    public LocalStateProvider(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
