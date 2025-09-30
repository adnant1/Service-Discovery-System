package com.github.adnant1.servicediscovery.gossip;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.github.adnant1.servicediscovery.util.ServiceInstanceSerializer;

@Component
public class LocalStateProvider {
 
    private final StringRedisTemplate redisTemplate;
    private final ServiceInstanceSerializer serializer;

    public LocalStateProvider(StringRedisTemplate redisTemplate, ServiceInstanceSerializer serializer) {
        this.redisTemplate = redisTemplate;
        this.serializer = serializer;
    }
}
