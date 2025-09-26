package com.github.adnant1.servicediscovery.grpc;

import org.springframework.stereotype.Component;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Creates and starts the gRPC server for the service registry.
 */
@Component
public class RegistryServer {

    private Server server;
    private static final int PORT = 50051;
    private final RegistryServiceImpl registryService;

    public RegistryServer(RegistryServiceImpl registryService) {
        this.registryService = registryService;
    }

    /**
     * Starts the gRPC server.
     * 
     * @throws Exception If the server fails to start.
     */
    @PostConstruct
    public void start() throws Exception {
        server = ServerBuilder.forPort(PORT)
                .addService(registryService)
                .build()
                .start();
        
        System.out.println("Server started, listening on " + PORT);

        // Shutdown hook so ctrl+c or kill signals stop the server gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server...");
            RegistryServer.this.stop();
            System.err.println("Server shut down.");
        }));
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
