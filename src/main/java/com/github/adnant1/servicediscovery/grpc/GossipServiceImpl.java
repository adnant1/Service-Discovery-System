package com.github.adnant1.servicediscovery.grpc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.github.adnant1.servicediscovery.identity.NodeIdentityProvider;
import com.github.adnant1.servicediscovery.registry.GossipRequest;
import com.github.adnant1.servicediscovery.registry.GossipResponse;
import com.github.adnant1.servicediscovery.registry.GossipServiceGrpc;
import com.github.adnant1.servicediscovery.registry.NodeInfo;
import com.github.adnant1.servicediscovery.registry.ServiceInstance;

import io.grpc.stub.StreamObserver;

/**
 * gRPC service implementation for handling gossip synchronization requests.
 * It processes incoming service instance data, updates the local Redis store,
 * and responds with the number of changes applied.
 */
@Service
public class GossipServiceImpl extends GossipServiceGrpc.GossipServiceImplBase {

    private final StringRedisTemplate redisTemplate;
    private final NodeIdentityProvider nodeIdentityProvider;

    public GossipServiceImpl(StringRedisTemplate redisTemplate, NodeIdentityProvider nodeIdentityProvider) {
        this.redisTemplate = redisTemplate;
        this.nodeIdentityProvider = nodeIdentityProvider;
    }
    
    /**
     * Handles incoming gossip synchronization requests.
     * It updates the local Redis store with the received service instances,
     * applying TTL and last-updated logic to determine whether to insert, overwrite, or ignore
     * each instance.
     * 
     * @param request the incoming gossip request containing service instances
     * @param responseObserver the observer to send the response back to the client
     */
    @Override
    public void sync(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        int changes = 0;
        long now = System.currentTimeMillis();

        // Service merge
        for (Map.Entry<String, ServiceInstance> entry: request.getInstancesMap().entrySet()) {
            String serviceKey = entry.getKey();
            ServiceInstance incomingInstance = entry.getValue();
            
            // Check if the incoming instance has expired (TTL is in seconds, convert to millis)
            if (now - incomingInstance.getLastUpdated() > (incomingInstance.getTtl() * 1000)) {
                redisTemplate.delete(serviceKey);
                continue;
            }

            // Fetch local instance from Redis hash
            Map<Object, Object> localFields = redisTemplate.opsForHash().entries(serviceKey);
            
            // Decide whether to insert/overwrite/ignore
            boolean shouldUpdate = false;
            if (localFields == null || localFields.isEmpty()) {
                shouldUpdate = true; // New instance
            } else {
                String localTsStr = (String) localFields.get("timestamp");
                if (localTsStr != null) {
                    long localTimestamp = Long.parseLong(localTsStr);
                    if (incomingInstance.getLastUpdated() > localTimestamp) {
                        shouldUpdate = true; // Incoming is newer
                    }
                }
            }

            if (shouldUpdate) {
                // Extract service name for the set
                String[] parts = serviceKey.split(":", 2);
                String serviceName = parts.length == 2 ? parts[0] : serviceKey;

                // Store as hash
                Map<String, String> fields = new HashMap<>();
                fields.put("ip", incomingInstance.getIp());
                fields.put("port", String.valueOf(incomingInstance.getPort()));
                fields.put("timestamp", String.valueOf(incomingInstance.getLastUpdated()));

                redisTemplate.opsForHash().putAll(serviceKey, fields);
                redisTemplate.expire(serviceKey, incomingInstance.getTtl(), TimeUnit.SECONDS);
                
                // Add to service set
                redisTemplate.opsForSet().add("service:" + serviceName, serviceKey);
                
                changes++;
            }
        }

        // Node merge
        for (Map.Entry<String, NodeInfo> entry: request.getNodesMap().entrySet()) {
            String nodeId = entry.getKey();
            NodeInfo info = entry.getValue();

            if (!nodeId.equals(nodeIdentityProvider.getNodeId())) {
                redisTemplate.opsForValue().set(
                    "node:" + nodeId,
                    String.valueOf(info.getLastUpdated()),
                    60, TimeUnit.SECONDS
                );
            }
        }

        GossipResponse response = GossipResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Applied " + changes + " updates")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
}
