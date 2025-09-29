package com.github.adnant1.servicediscovery.identity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class IdentityConfig {

    @Bean
    public Integer grpcPort() {
        return 50051;
    }

    @Bean
    public NodeIdentityProvider nodeIdentityProvider(StringRedisTemplate redisTemplate, Integer grpcPort) {
        return new NodeIdentityProvider(redisTemplate, grpcPort);
    }
}
