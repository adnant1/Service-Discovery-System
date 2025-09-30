package com.github.adnant1.servicediscovery.grpc;

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
import com.github.adnant1.servicediscovery.util.ServiceInstanceSerializer;

import io.grpc.stub.StreamObserver;

/**
 * gRPC service implementation for handling gossip synchronization requests.
 * It processes incoming service instance data, updates the local Redis store,
 * and responds with the number of changes applied.
 */
@Service
public class GossipServiceImpl extends GossipServiceGrpc.GossipServiceImplBase {

    private final StringRedisTemplate redisTemplate;
    private final ServiceInstanceSerializer serializer;
    private final NodeIdentityProvider nodeIdentityProvider;

    public GossipServiceImpl(StringRedisTemplate redisTemplate, ServiceInstanceSerializer serializer, 
                           NodeIdentityProvider nodeIdentityProvider) {
        this.redisTemplate = redisTemplate;
        this.serializer = serializer;
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
            String serviceId = entry.getKey();
            ServiceInstance incomingInstance = entry.getValue();
            
            // Check if the incoming instance has expired
            if (now - incomingInstance.getLastUpdated() > incomingInstance.getTtl()) {
                redisTemplate.delete(serviceId);
                continue;
            }

            // Fetch local instance from Redis
            // Deserializes the JSON string back to ServiceInstance using a built in protobuf method
            String localJson = redisTemplate.opsForValue().get(serviceId);
            ServiceInstance localInstance = serializer.deserialize(localJson);

            // Decide whether to insert/overwrite/ignore
            if (localInstance == null || incomingInstance.getLastUpdated() > localInstance.getLastUpdated()) {
                // Serialize the ServiceInstance to a JSON string using a built in protobuf method
                String serialized = serializer.serialize(incomingInstance);
                redisTemplate.opsForValue().set(serviceId, serialized, incomingInstance.getTtl(), TimeUnit.SECONDS);
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
