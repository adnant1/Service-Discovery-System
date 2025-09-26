package com.github.adnant1.servicediscovery.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    } 

    /**
     * Deletes a service instance from Redis.
     * 
     * @param serviceName the name of the service
     * @param instanceId the unique ID of the service instance
     * @return true if the instance was deleted, false if it did not exist
     */
    public boolean deleteInstance(String serviceName, String instanceId) {
        return false; // Placeholder return
    }

    /**
     * Retrieves all instances of a given service from Redis.
     * 
     * @param serviceName the name of the service
     * @return a list of type ServiceInstance
     */
    public List<ServiceInstance> getInstances(String serviceName) {
        return List.of(); // Placeholder return
    }

    /**
     * Refreshes the TTL of a service instance in Redis.
     * 
     * @param serviceName the name of the service
     * @param instanceId the unique ID of the service instance
     */
    public void refreshTtl(String serviceName, String instanceId) {

    }
}   
