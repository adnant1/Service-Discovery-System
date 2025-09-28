package com.github.adnant1.servicediscovery.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import com.github.adnant1.servicediscovery.grpc.GrpcPortAllocator;

public class IdentityConfig {

    private static final Logger logger = LoggerFactory.getLogger(IdentityConfig.class);

    @Bean
    public Integer grpcPort() {
        int port = GrpcPortAllocator.assignGrpcPort();
        logger.info("[BOOT] Assigned gRPC port: {}", port);
        return port;
    }
}
