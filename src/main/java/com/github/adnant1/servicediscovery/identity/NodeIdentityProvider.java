package com.github.adnant1.servicediscovery.identity;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Obtains the unique ID of the node from Redis.
 * If an identity does not exist, it creates one based on the node's hostname:port and a UUID.
 */
public class NodeIdentityProvider {
    
    private final StringRedisTemplate redisTemplate;
    private final String nodeId;

    public NodeIdentityProvider(StringRedisTemplate redisTemplate, int port) {
        this.redisTemplate = redisTemplate;
        this.nodeId = getOrCreateNodeId(port);
    }

    public String getNodeId() {
        return nodeId;
    }

    /**
     * Generates a unique key for storing the node ID in Redis.
     * ID = hostname:port-UUID(6 chars)
     * 
     * @param port the port number of the node
     * @return the generated key
     */
    private String generateNodeId(int port) {
        String uuid = java.util.UUID.randomUUID().toString().substring(0, 6);

        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return hostname + ":" + port + "-" + uuid;
        } catch (UnknownHostException e) {
            // Fallback to localhost if hostname cannot be determined
            return "localhost:" + port + "-" + uuid;
        }
    }

}
