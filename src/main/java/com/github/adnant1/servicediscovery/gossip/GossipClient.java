package com.github.adnant1.servicediscovery.gossip;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.adnant1.servicediscovery.registry.GossipRequest;
import com.github.adnant1.servicediscovery.registry.GossipResponse;
import com.github.adnant1.servicediscovery.registry.GossipServiceGrpc;
import com.github.adnant1.servicediscovery.registry.NodeInfo;
import com.github.adnant1.servicediscovery.registry.ServiceInstance;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Client for communicating with peer nodes in the gossip protocol.
 * Opens channels to a given peer, builds a GossipRequest, and invokes 
 * that peer's sync gRPC method.
 */
@Component
public class GossipClient {

    /**
     * Sends local state to a peer node using the GossipService sync RPC.
     * 
     * @param peerAddress the hostname:port of the peer node
     * @param localState a map of instanceId -> ServiceInstance representing local state
     * @return GossipResponse from the peer node
     */
    public GossipResponse sync(String peerAddress, Map<String, ServiceInstance> localState, Map<String, NodeInfo> nodes) {
        String[] parts = peerAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        // Build the gRPC channel + stub
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        GossipServiceGrpc.GossipServiceBlockingStub stub = GossipServiceGrpc.newBlockingStub(channel);

        // Build the request and call the RPC
        GossipRequest request = GossipRequest.newBuilder()
                .putAllInstances(localState)
                .putAllNodes(nodes)
                .build();
        
        GossipResponse response = stub.sync(request);

        // Shutdown the channel
        channel.shutdown();

        return response;
    }
    
}
