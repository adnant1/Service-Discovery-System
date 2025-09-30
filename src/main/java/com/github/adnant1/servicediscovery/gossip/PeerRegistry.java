package com.github.adnant1.servicediscovery.gossip;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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

    /**
     * Return a random peer node ID, excluding the current node's ID.
     * 
     * @return a random peer node ID, or null if no peers are available
     */
    public String pickRandomPeer() {
        Set<String> allNodes = redisTemplate.opsForSet().members("nodes");
        if (allNodes == null || allNodes.isEmpty()) {
            return null;
        }

        String selfId = nodeIdentityProvider.getNodeId();
        List<String> peers = allNodes.stream().filter(id -> !id.equals(selfId)).toList();

        if (peers.isEmpty()) {
            return null;
        }

        int idx = ThreadLocalRandom.current().nextInt(peers.size());
        return peers.get(idx);
    }
    
}
