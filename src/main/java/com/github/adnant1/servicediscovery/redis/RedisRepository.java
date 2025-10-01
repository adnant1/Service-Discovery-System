package com.github.adnant1.servicediscovery.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.github.adnant1.servicediscovery.registry.ServiceInstance;

/**
 * Repository class for managing service registry data in Redis.
 */
@Repository
public class RedisRepository {
    private final StringRedisTemplate redisTemplate;
    private final int ttlSeconds;

    public RedisRepository(StringRedisTemplate redisTemplate, 
                          @Value("${spring.ttl-seconds:30}") int ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * Saves a service instance to Redis with a TTL.
     * 
     * @param serviceName the name of the service
     * @param instanceId the unique ID of the service instance
     * @param ip the IP address of the service instance
     * @param port the port number of the service instance
     */
    public void saveInstance(String serviceName, String instanceId, String ip, int port) {
        String key = serviceName + ":" + instanceId;

        Map<String, String> fields = new HashMap<>();
        fields.put("ip", ip);
        fields.put("port", String.valueOf(port));
        fields.put("timestamp", String.valueOf(System.currentTimeMillis()));

        redisTemplate.opsForHash().putAll(key, fields);
        redisTemplate.expire(key, java.time.Duration.ofSeconds(ttlSeconds));

        // Add to services set for easy lookup
        redisTemplate.opsForSet().add("service:" + serviceName, key);

        // Add to global services set
        redisTemplate.opsForSet().add("allServices", key);
    } 

    /**
     * Deletes a service instance from Redis.
     * 
     * @param serviceName the name of the service
     * @param instanceId the unique ID of the service instance
     * @return true if the instance was deleted, false if it did not exist
     */
    public boolean deleteInstance(String serviceName, String instanceId) {
        String key = serviceName + ":" + instanceId;
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Retrieves all instances of a given service from Redis.
     * 
     * @param serviceName the name of the service
     * @return a list of type ServiceInstance
     */
    public List<ServiceInstance> getInstances(String serviceName) {
        Set<String> keys = redisTemplate.opsForSet().members("service:" + serviceName);

        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        // Map each key to a ServiceInstance object
        List<ServiceInstance> instances = new ArrayList<>();
        for (String key: keys) {
            Map<Object, Object> fields = redisTemplate.opsForHash().entries(key);

            String ip = (String) fields.get("ip");
            String portStr = (String) fields.get("port");

            String[] parts = key.split(":", 2);
            String instanceId = (parts.length == 2) ? parts[1] : "";

            if (ip != null && portStr != null && !instanceId.isEmpty()) {
                int port = Integer.parseInt(portStr);
                ServiceInstance instance = ServiceInstance.newBuilder()
                        .setInstanceId(instanceId)
                        .setIp(ip)
                        .setPort(port)
                        .build();
                
                instances.add(instance);
            }
        }

        return instances;
    }

    /**
     * Refreshes the TTL of a service instance in Redis.
     * 
     * @param serviceName the name of the service
     * @param instanceId the unique ID of the service instance
     * @return true if the TTL was refreshed, false if the instance does not exist
     */
    public boolean refreshTtl(String serviceName, String instanceId) {
        String key = serviceName + ":" + instanceId;

        Boolean exists = redisTemplate.hasKey(key);
        if (exists == null || !exists) {
            return false;
        }

        redisTemplate.expire(key, java.time.Duration.ofSeconds(ttlSeconds));
        return true;
    }
}   
