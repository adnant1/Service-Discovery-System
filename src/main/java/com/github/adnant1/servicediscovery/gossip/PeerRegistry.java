package com.github.adnant1.servicediscovery.gossip;

import java.util.ArrayList;
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
    private final List<String> seedPeers;

    public PeerRegistry(StringRedisTemplate redisTemplate, NodeIdentityProvider nodeIdentityProvider, List<String> seedPeers) {
        this.redisTemplate = redisTemplate;
        this.nodeIdentityProvider = nodeIdentityProvider;
        this.seedPeers = seedPeers;
    }

    /**
     * Return a random peer node ID, excluding the current node's ID.
     * 
     * @return a random peer node ID, or null if no peers are available
     */
    public String pickRandomPeer() {
        String self = nodeIdentityProvider.getNodeId();
        List<String> peers = new ArrayList<>();

        // Discover peers from local Redis
        Set<String> keys = redisTemplate.keys("node:*");
        if (keys != null) {
            for (String key : keys) {
                String nodeId = redisTemplate.opsForValue().get(key);
                if (nodeId != null && !nodeId.equals(self)) {
                    peers.add(nodeId);
                }
            }
        }

        // Fallback to seed peers if no peers found
        if (peers.isEmpty()) {
            for (String seed : seedPeers) {
                if (!seed.equals(self)) {
                    peers.add(seed);
                }
            }
        }

        if (peers.isEmpty()) {
            return null; // No peers available
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(peers.size());
        return extractAddress(peers.get(randomIndex));
    }

    /**
     * Extracts the address (hostname:port) from a node ID.
     * 
     * @param nodeId the full node ID
     * @return the address portion of the node ID
     */
    private String extractAddress(String nodeId) {
        int dashIndex = nodeId.indexOf('-');

        if (dashIndex == -1) {
            return nodeId;
        }

        return nodeId.substring(0, dashIndex);
    }
}
