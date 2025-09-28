package com.github.adnant1.servicediscovery.grpc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;

/**
 * Creates and starts the gRPC server for the service registry.
 */
@Profile("!test")
@Component
public class RegistryServer implements CommandLineRunner {

    private Server server;
    private final int grpcPort;
    private final RegistryServiceImpl registryService;

    public RegistryServer(RegistryServiceImpl registryService, Integer grpcPort) {
        this.registryService = registryService;
        this.grpcPort = grpcPort;
    }

    @Override
    public void run(String... args) throws Exception {
        server = ServerBuilder.forPort(grpcPort)
                .addService(registryService)
                .build()
                .start();
        
        System.out.println("Server started, listening on " + PORT);

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
