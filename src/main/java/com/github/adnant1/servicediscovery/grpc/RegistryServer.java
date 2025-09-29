package com.github.adnant1.servicediscovery.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.adnant1.servicediscovery.identity.NodeIdentityProvider;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;

/**
 * Creates and starts the gRPC server for the service registry.
 */
@Profile("!test")
@Component
public class RegistryServer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RegistryServer.class);

    private Server server;
    private final Integer grpcPort;
    private final RegistryServiceImpl registryService;
    private final NodeIdentityProvider nodeIdentityProvider;

    public RegistryServer(RegistryServiceImpl registryService, NodeIdentityProvider nodeIdentityProvider, Integer grpcPort) {
        this.registryService = registryService;
        this.nodeIdentityProvider = nodeIdentityProvider;
        this.grpcPort = grpcPort;
    }

    @Override
    public void run(String... args) throws Exception {
        String nodeId = nodeIdentityProvider.getNodeId();
        logger.info("[BOOT] Node identity established: {}", nodeId);

        server = ServerBuilder.forPort(grpcPort)
                .addService(registryService)
                .build()
                .start();
        
        logger.info("[BOOT] gRPC server started, listening on {}", grpcPort);

        server.awaitTermination();
    }

    /**
     * Stops the gRPC server.
     */
    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
