package com.github.adnant1.servicediscovery.gossip;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.github.adnant1.servicediscovery.identity.NodeIdentityProvider;

/**
 * Registry for managing peer nodes in the gossip protocol.
 */
@Component
public class PeerRegistry {

    private final StringRedisTemplate redisTemplate;
    private final NodeIdentityProvider nodeIdentityProvider;

    public PeerRegistry(StringRedisTemplate redisTemplate, NodeIdentityProvider nodeIdentityProvider) {
        this.redisTemplate = redisTemplate;
        this.nodeIdentityProvider = nodeIdentityProvider;
    }
    
}
