package com.github.adnant1.servicediscovery.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.adnant1.servicediscovery.registry.ServiceInstance;

/**
 * Utility class for serializing and deserializing ServiceInstance objects.
 */
@Component
public class ServiceInstanceSerializer {
    
    private final ObjectMapper objectMapper;

    public ServiceInstanceSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Serializes a ServiceInstance object to a JSON string.
     * 
     * @param instance the ServiceInstance to serialize
     * @return the JSON string representation of the ServiceInstance
     */
    public String serialize(ServiceInstance instance) {
        try {
            return objectMapper.writeValueAsString(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ServiceInstance", e);
        }
    }

    /**
     * Deserializes a JSON string to a ServiceInstance object.
     * 
     * @param json the JSON string to deserialize
     * @return the deserialized ServiceInstance object
     */
    public ServiceInstance deserialize(String json) {
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, ServiceInstance.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ServiceInstance", e);
        }
    }
}
