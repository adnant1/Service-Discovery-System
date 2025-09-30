package com.github.adnant1.servicediscovery.gossip;

import org.springframework.stereotype.Component;

/**
 * Scheduler that runs gossip rounds periodically for eventual consistency.
 */
@Component
public class GossipScheduler {

    private final PeerRegistry peerRegistry;
    private final LocalStateProvider localStateProvider;
    private final GossipClient gossipClient;

    public GossipScheduler(PeerRegistry peerRegistry, LocalStateProvider localStateProvider, GossipClient gossipClient) {
        this.peerRegistry = peerRegistry;
        this.localStateProvider = localStateProvider;
        this.gossipClient = gossipClient;
    }
    
}
