package com.github.adnant1.servicediscovery.gossip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.adnant1.servicediscovery.registry.GossipResponse;

/**
 * Scheduler that runs gossip rounds periodically for eventual consistency.
 */
@Component
public class GossipScheduler {

    private final Logger logger = LoggerFactory.getLogger(GossipScheduler.class);

    private final PeerRegistry peerRegistry;
    private final LocalStateProvider localStateProvider;
    private final GossipClient gossipClient;

    public GossipScheduler(PeerRegistry peerRegistry, LocalStateProvider localStateProvider, GossipClient gossipClient) {
        this.peerRegistry = peerRegistry;
        this.localStateProvider = localStateProvider;
        this.gossipClient = gossipClient;
    }

    /**
     * Runs a gossip round by picking a random peer, dumping local state,
     * and invoking the gossip client to sync state with the peer.
     * Scheduled to run every 5 seconds.
     */
    @Scheduled(fixedRate = 5000)
    public void runGossipRound() {
        String peer = peerRegistry.pickRandomPeer();
        if (peer == null) {
            return; // No peers available
        }

        var localState = localStateProvider.dumpServices();
        try {
            GossipResponse response = gossipClient.sync(peer, localState);
            logger.info("Gossip sync with peer {} succeeded: {}", peer, response.getMessage());
        } catch (Exception e) {
            // Log and ignore errors to avoid disrupting future gossip rounds
            logger.warn("Gossip sync with peer {} failed: {}", peer, e.getMessage());
        }

    }
}
