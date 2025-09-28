package com.github.adnant1.servicediscovery.grpc;

/**
 * Handles gRPC port allocation for the service registry.
 * Starts at the default port 50051 and increments if the port is in use.
 */
public class GrpcPortAllocator {

    private static final int START_PORT = 50051;

    /**
     * Returns a gRPC port for the node to use.
     * Increments from the starting port if the port is already in use.
     * 
     * @return an available gRPC port
     */
    public static int assignGrpcPort() {
        int port = START_PORT;
        while (!isFree(port)) {
            port++;
        }

        return port;
    }
    
}
