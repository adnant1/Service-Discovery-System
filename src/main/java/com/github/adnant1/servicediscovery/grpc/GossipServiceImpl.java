package com.github.adnant1.servicediscovery.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.adnant1.servicediscovery.registry.GossipRequest;
import com.github.adnant1.servicediscovery.registry.GossipResponse;
import com.github.adnant1.servicediscovery.registry.GossipServiceGrpc;

import io.grpc.stub.StreamObserver;

@Service
public class GossipServiceImpl extends GossipServiceGrpc.GossipServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GossipServiceImpl.class);
    
    @Override
    public void sync(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        // For now log sender + how many services and return ACK
        logger.info("Received gossip sync with {} instances", request.getInstancesCount());
        GossipResponse response = GossipResponse.newBuilder()
                .setSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
}
