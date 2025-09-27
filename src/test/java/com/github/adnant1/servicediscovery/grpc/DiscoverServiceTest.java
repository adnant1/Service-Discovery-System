package com.github.adnant1.servicediscovery.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.adnant1.servicediscovery.TestStreamObserver;
import com.github.adnant1.servicediscovery.redis.RedisRepository;
import com.github.adnant1.servicediscovery.registry.DiscoverRequest;
import com.github.adnant1.servicediscovery.registry.DiscoverResponse;
import com.github.adnant1.servicediscovery.registry.ServiceInstance;

import io.grpc.StatusRuntimeException;

public class DiscoverServiceTest {
    
    private RedisRepository redisRepository;
    private RegistryServiceImpl service;

    @BeforeEach
    public void setUp() {
        this.redisRepository = Mockito.mock(RedisRepository.class);
        this.service = new RegistryServiceImpl(redisRepository);
    }

    @Test
    void testDiscoverWithInstances() {
        // Create
        ServiceInstance instance = ServiceInstance.newBuilder()
                .setInstanceId("instance-1")
                .setIp("10.0.0.1")
                .setPort(8080)
                .build();
        
        Mockito.when(redisRepository.getInstances("auth-service"))
            .thenReturn(List.of(instance));
        
        DiscoverRequest request = DiscoverRequest.newBuilder()
                .setServiceName("auth-service")
                .build();
        
        TestStreamObserver<DiscoverResponse> observer = new TestStreamObserver<>();

        // Act
        service.discover(request, observer);

        // Assert
        assertNull(observer.getError(), "No error should occur");
        assertTrue(observer.isCompleted());

        DiscoverResponse response = observer.getResponse();
        assertNotNull(response);
        assertEquals(1, response.getInstancesCount());
        assertEquals("instance-1", response.getInstances(0).getInstanceId());
        assertEquals("10.0.0.1", response.getInstances(0).getIp());
        assertEquals(8080, response.getInstances(0).getPort());
    }

    @Test
    void testDiscoverNoInstances() {
        // Create
        Mockito.when(redisRepository.getInstances("auth-service"))
            .thenReturn(List.of());
        
        DiscoverRequest request = DiscoverRequest.newBuilder()
                .setServiceName("auth-service")
                .build();
        
        TestStreamObserver<DiscoverResponse> observer = new TestStreamObserver<>();

        // Act
        service.discover(request, observer);

        // Assert
        assertNull(observer.getError(), "No error should occur");
        assertTrue(observer.isCompleted());

        DiscoverResponse response = observer.getResponse();
        assertNotNull(response);
        assertEquals(0, response.getInstancesCount());
    }

    @Test
    void testDiscoverMissingServiceName() {
        // Create
        DiscoverRequest request = DiscoverRequest.newBuilder()
                .setServiceName("")
                .build();
        
        TestStreamObserver<DiscoverResponse> observer = new TestStreamObserver<>();

        // Act
        service.discover(request, observer);

        // Assert
        assertNotNull(observer.getError(), "Error should occur");
        assertNull(observer.getResponse(), "No response should be returned");

        Throwable error = observer.getError();
        assertInstanceOf(StatusRuntimeException.class, error);

        StatusRuntimeException statusEx = (StatusRuntimeException) error;
        assertEquals(io.grpc.Status.INVALID_ARGUMENT.getCode(), statusEx.getStatus().getCode());
        assertTrue(statusEx.getStatus().getDescription().contains("Service name cannot be empty."));
    }
}
