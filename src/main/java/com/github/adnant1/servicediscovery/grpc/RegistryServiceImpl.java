package com.github.adnant1.servicediscovery.grpc;

import java.util.List;

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
import com.github.adnant1.servicediscovery.registry.ServiceInstance;

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
                responseObserver.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("Service name cannot be empty.")
                        .asRuntimeException()
                );
                return;
            }

            if (port <= 0 || port > 65535) {
                responseObserver.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("Invalid port number: " + port)
                        .asRuntimeException()
                );
                return;
            }

            if (instanceId == null || instanceId.isEmpty()) {
                responseObserver.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("Instance ID cannot be empty.")
                        .asRuntimeException()
                );
                return;
            }

            if (ip == null || ip.isEmpty()) {
                responseObserver.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("IP address cannot be empty.")
                        .asRuntimeException()
                );
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
        String serviceName = request.getServiceName();
        String instanceId = request.getInstanceId();

        try {
            // Input validation
            if (serviceName == null || serviceName.isEmpty()) {
                DeregisterResponse response = DeregisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Service name cannot be empty.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            if (instanceId == null || instanceId.isEmpty()) {
                DeregisterResponse response = DeregisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Instance ID cannot be empty.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Delete the service instance (if it exists) from Redis and build the corresponding response
            boolean deleted = redisRepository.deleteInstance(serviceName, instanceId);
            DeregisterResponse response;

            if (deleted) {
                response = DeregisterResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Service deregistered successfully.")
                        .build();
            } else {
                response = DeregisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Service instance not found.")
                        .build();
            }
            
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
     * Handles service discovery requests.
     * 
     * @param request The discovery request containing criteria for service lookup.
     * @param responseObserver The observer to send the discovery response.
     */
    @Override
    public void discover(DiscoverRequest request, StreamObserver<DiscoverResponse> responseObserver) {
        String serviceName = request.getServiceName();

        try {
            // Input validation
            if (serviceName == null || serviceName.isEmpty()) {
                responseObserver.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("Service name cannot be empty.")
                        .asRuntimeException()
                );
                return;
            }

            // Retrieve service instances from Redis and build the corresponding response
            List<ServiceInstance> instances = redisRepository.getInstances(serviceName);

            DiscoverResponse response = DiscoverResponse.newBuilder()
                    .addAllInstances(instances)
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
     * Handles heartbeat requests to keep services alive in the registry.
     * 
     * @param request The heartbeat request containing service details.
     * @param responseObserver The observer to send the heartbeat response.
     */
    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        String serviceName = request.getServiceName();
        String instanceId = request.getInstanceId();

        try {
            // Input validation
            if (serviceName == null || serviceName.isEmpty()) {
                HeartbeatResponse response = HeartbeatResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Service name cannot be empty.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            if (instanceId == null || instanceId.isEmpty()) {
                HeartbeatResponse response = HeartbeatResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Instance ID cannot be empty.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Refresh the TTL of the service instance in Redis and build the corresponding response
            boolean refreshed = redisRepository.refreshTtl(serviceName, instanceId);
            HeartbeatResponse response;

            if (refreshed) {
                response = HeartbeatResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Heartbeat received, TTL refreshed.")
                        .build();
            } else {
                response = HeartbeatResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Service instance not found.")
                        .build();
            }

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
}
