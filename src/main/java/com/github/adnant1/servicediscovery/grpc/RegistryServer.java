package com.github.adnant1.servicediscovery.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Creates and starts the gRPC server for the service registry.
 */
public class RegistryServer {

    private Server server;
    private static final int PORT = 50051;

    /**
     * Starts the gRPC server.
     * 
     * @throws Exception If the server fails to start.
     */
    public void start() throws Exception {
        server = ServerBuilder.forPort(PORT)
                .addService(new RegistryServiceImpl())
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
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Block the main thread until the server is terminated.
     * 
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main method to start the server.
     * 
     * @param args Command line arguments (not used).
     * @throws Exception If the server fails to start.
     */
    public static void main(String[] args) throws Exception {
        final RegistryServer server = new RegistryServer();
        server.start();
        server.blockUntilShutdown();
    }
}
