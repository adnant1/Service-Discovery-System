package com.github.adnant1.servicediscovery.gossip;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.adnant1.servicediscovery.registry.GossipResponse;
import com.github.adnant1.servicediscovery.registry.ServiceInstance;

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
     * @param peerAddress the address of the peer node
     * @param localState a map of instanceId -> ServiceInstance representing local state
     * @return GossipResponse from the peer node
     */
    public GossipResponse sync(String peerAddress, Map<String, ServiceInstance> localState) {

    }
    
}
