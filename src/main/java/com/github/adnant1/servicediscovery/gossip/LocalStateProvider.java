package com.github.adnant1.servicediscovery.gossip;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.github.adnant1.servicediscovery.registry.NodeInfo;
import com.github.adnant1.servicediscovery.registry.ServiceInstance;

/**
 * Provider for dumping local service state from Redis.
 */
@Component
public class LocalStateProvider {
 
    private final StringRedisTemplate redisTemplate;

    public LocalStateProvider(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Dumps all service instances stored in local Redis.
     * 
     * @return a map of service IDs to their corresponding ServiceInstance objects
     */
    public Map<String, ServiceInstance> dumpServices() {
        Map<String, ServiceInstance> services = new HashMap<>();

        // Retrieve all service sets
        Set<String> serviceKeys = redisTemplate.keys("service:*");
        if (serviceKeys == null || serviceKeys.isEmpty()) {
            return services;
        }

        for (String serviceKey: serviceKeys) {
            // For each service set, get its instance keys
            Set<String> keys = redisTemplate.opsForSet().members(serviceKey);
            if (keys == null || keys.isEmpty()) {
                continue;
            }

            // For each instance key, retrieve its details
            for (String key : keys) {
                Map<Object, Object> fields = redisTemplate.opsForHash().entries(key);
                if (fields == null || fields.isEmpty()) {
                    continue;
                }

                String[] parts = key.split(":", 2);
                if (parts.length != 2) {
                    continue; // Invalid key format
                }
    
                String instanceId = parts[1];
                String ip = (String) fields.get("ip");
                String portStr = (String) fields.get("port");
                String tsStr = (String) fields.get("timestamp");
                int port = Integer.parseInt(portStr);
                long lastUpdated = Long.parseLong(tsStr);
    
                Long ttlRemaining = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttlRemaining == null || ttlRemaining <= 0) {
                    continue; // Skip expired instances
                }
    
                ServiceInstance instance = ServiceInstance.newBuilder()
                        .setInstanceId(instanceId)
                        .setIp(ip)
                        .setPort(port)
                        .setTtl(ttlRemaining)
                        .setLastUpdated(lastUpdated)
                        .build();

                services.put(key, instance);
            }
        }

        return services;
    }

    /**
     * Dumps all known nodes stored in local Redis.
     * 
     * @return a map of node IDs to their corresponding NodeInfo objects
     */
    public Map<String, NodeInfo> dumpNodes() {
        Map<String, NodeInfo> nodes = new HashMap<>();
        Set<String> keys = redisTemplate.keys("node:*");
        if (keys == null || keys.isEmpty()) {
            return nodes;   
        }

        for (String key: keys) {
            String nodeId = key.substring("node:".length());

            NodeInfo info = NodeInfo.newBuilder()
                    .setNodeId(nodeId)
                    .setLastUpdated(System.currentTimeMillis())
                    .build();
            
            nodes.put(nodeId, info);
        }

        return nodes;
    }
}
