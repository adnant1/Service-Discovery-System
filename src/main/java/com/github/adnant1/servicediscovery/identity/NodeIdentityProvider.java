package com.github.adnant1.servicediscovery.identity;

import org.springframework.data.redis.core.StringRedisTemplate;

public class NodeIdentityProvider {
    
    private final StringRedisTemplate redisTemplate;
    private final String nodeId;

    public NodeIdentityProvider(StringRedisTemplate redisTemplate, int port) {
        this.redisTemplate = redisTemplate;
        this.nodeId = getOrCreateNodeId(port);
    }


}
