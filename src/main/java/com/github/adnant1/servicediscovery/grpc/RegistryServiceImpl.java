package com.github.adnant1.servicediscovery.grpc;

import org.springframework.stereotype.Service;

import com.github.adnant1.servicediscovery.redis.RedisRepository;
import com.github.adnant1.servicediscovery.registry.DeregisterRequest;
import com.github.adnant1.servicediscovery.registry.DeregisterResponse;
import com.github.adnant1.servicediscovery.registry.DiscoverRequest;
import com.github.adnant1.servicediscovery.registry.DiscoverResponse;
import com.github.adnant1.servicediscovery.registry.HeartbeatRequest;
import com.github.adnant1.servicediscovery.registry.HeartbeatResponse;
import com.github.adnant1.servicediscovery.registry.RegisterRequest;
import com.github.adnant1.servicediscovery.registry.RegisterResponse;
import com.github.adnant1.servicediscovery.registry.RegistryServiceGrpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * Implementation of the gRPC service methods for service registry operations.
 */
@Service
public class RegistryServiceImpl extends RegistryServiceGrpc.RegistryServiceImplBase {

    private final RedisRepository redisRepository;

    public RegistryServiceImpl(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    /**
     * Handles service registration requests.
     * 
     * @param request The registration request containing service details.
     * @param responseObserver The observer to send the registration response.
     */
    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        String serviceName = request.getServiceName();
        String instanceId = request.getInstanceId();
        String ip = request.getIp();
        int port = request.getPort();

        try {
            // Input validation
            if (serviceName == null || serviceName.isEmpty()) {
                RegisterResponse response = RegisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Service name cannot be empty.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            if (port <= 0 || port > 65535) {
                RegisterResponse response = RegisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Invalid port number: " + port)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            if (instanceId == null || instanceId.isEmpty()) {
                RegisterResponse response = RegisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Instance ID cannot be empty.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            if (ip == null || ip.isEmpty()) {
                RegisterResponse response = RegisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("IP address cannot be empty.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Save the service instance to Redis and build success response
            redisRepository.saveInstance(serviceName, instanceId, ip, port);

            RegisterResponse response = RegisterResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Service registered successfully.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            // Return gRPC error if Redis operation fails
            responseObserver.onError(
                Status.UNAVAILABLE
                    .withDescription("Redis unavailable: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        }
    }

    /**
     * Handles service deregistration requests.
     * 
     * @param request The deregistration request containing service details.
     * @param responseObserver The observer to send the deregistration response.
     */
    @Override
    public void deregister(DeregisterRequest request, StreamObserver<DeregisterResponse> responseObserver) {
        // Unimplemented method stub
    }

    /**
     * Handles service discovery requests.
     * 
     * @param request The discovery request containing criteria for service lookup.
     * @param responseObserver The observer to send the discovery response.
     */
    @Override
    public void discover(DiscoverRequest request, StreamObserver<DiscoverResponse> responseObserver) {
        // Unimplemented method stub
    }

    /**
     * Handles heartbeat requests to keep services alive in the registry.
     * 
     * @param request The heartbeat request containing service details.
     * @param responseObserver The observer to send the heartbeat response.
     */
    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        // Unimplemented method stub
    }
}
