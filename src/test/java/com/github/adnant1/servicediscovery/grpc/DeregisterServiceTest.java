package com.github.adnant1.servicediscovery.grpc;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.github.adnant1.servicediscovery.redis.RedisRepository;

public class DeregisterServiceTest {
    
    private RedisRepository redisRepository;
    private RegistryServiceImpl service;

    @BeforeEach
    public void setUp() {
        this.redisRepository = Mockito.mock(RedisRepository.class);
        this.service = new RegistryServiceImpl(redisRepository);
    }
}
