package com.github.adnant1.servicediscovery.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.adnant1.servicediscovery.grpc.GrpcPortAllocator;

@Configuration
public class IdentityConfig {

    private static final Logger logger = LoggerFactory.getLogger(IdentityConfig.class);

    @Bean
    public Integer grpcPort() {
        int port = GrpcPortAllocator.assignGrpcPort();
        logger.info("[BOOT] Assigned gRPC port: {}", port);
        return port;
    }

    @Bean
    public NodeIdentityProvider nodeIdentityProvider(StringRedisTemplate redisTemplate, Integer grpcPort) {
        return new NodeIdentityProvider(redisTemplate, grpcPort);
    }
}
