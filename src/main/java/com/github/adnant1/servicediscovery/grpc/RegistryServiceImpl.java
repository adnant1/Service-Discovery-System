package com.github.adnant1.servicediscovery.grpc;

import com.github.adnant1.servicediscovery.registry.DeregisterRequest;
import com.github.adnant1.servicediscovery.registry.DeregisterResponse;
import com.github.adnant1.servicediscovery.registry.DiscoverRequest;
import com.github.adnant1.servicediscovery.registry.DiscoverResponse;
import com.github.adnant1.servicediscovery.registry.HeartbeatRequest;
import com.github.adnant1.servicediscovery.registry.HeartbeatResponse;
import com.github.adnant1.servicediscovery.registry.RegisterRequest;
import com.github.adnant1.servicediscovery.registry.RegisterResponse;
import com.github.adnant1.servicediscovery.registry.RegistryServiceGrpc;

import io.grpc.stub.StreamObserver;

public class RegistryServiceImpl extends RegistryServiceGrpc.RegistryServiceImplBase {

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        // Unimplemented method stub
    }

    @Override
    public void deregister(DeregisterRequest request, StreamObserver<DeregisterResponse> responseObserver) {
        // Unimplemented method stub
    }

    @Override
    public void discover(DiscoverRequest request, StreamObserver<DiscoverResponse> responseObserver) {
        // Unimplemented method stub
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        // Unimplemented method stub
    }
}
