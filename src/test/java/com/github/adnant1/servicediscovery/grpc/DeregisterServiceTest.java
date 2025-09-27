package com.github.adnant1.servicediscovery.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.adnant1.servicediscovery.TestStreamObserver;
import com.github.adnant1.servicediscovery.redis.RedisRepository;
import com.github.adnant1.servicediscovery.registry.DeregisterRequest;
import com.github.adnant1.servicediscovery.registry.DeregisterResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class DeregisterServiceTest {
    
    private RedisRepository redisRepository;
    private RegistryServiceImpl service;

    @BeforeEach
    public void setUp() {
        this.redisRepository = Mockito.mock(RedisRepository.class);
        this.service = new RegistryServiceImpl(redisRepository);
    }

    @Test
    void testDeregisterSuccess() {
        // Create
        DeregisterRequest request = DeregisterRequest.newBuilder()
                .setServiceName("auth-service")
                .setInstanceId("instance-1")
                .build();

        Mockito.when(redisRepository.deleteInstance("auth-service", "instance-1"))
                .thenReturn(true);

        TestStreamObserver<DeregisterResponse> observer = new TestStreamObserver<>();

        // Act
        service.deregister(request, observer);

        // Assert
        assertNull(observer.getError(), "No error expected");
        assertTrue(observer.isCompleted(), "Call should complete");

        DeregisterResponse response = observer.getResponse();
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Service deregistered successfully.", response.getMessage());
    }

    @Test
    void testDeregisterNonexistentInstance() {
        // Create
        DeregisterRequest request = DeregisterRequest.newBuilder()
                .setServiceName("auth-service")
                .setInstanceId("does-not-exist")
                .build();

        Mockito.when(redisRepository.deleteInstance("auth-service", "does-not-exist"))
                .thenReturn(false);

        TestStreamObserver<DeregisterResponse> observer = new TestStreamObserver<>();

        // Act
        service.deregister(request, observer);

        // Assert
        DeregisterResponse response = observer.getResponse();
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Service instance not found.", response.getMessage());
    }

    @Test
    void testDeregisterInvalidInput() {
        DeregisterRequest request = DeregisterRequest.newBuilder()
                .setServiceName("") // invalid
                .setInstanceId("instance-1")
                .build();

        TestStreamObserver<DeregisterResponse> observer = new TestStreamObserver<>();
        service.deregister(request, observer);

        assertNull(observer.getResponse(), "No response expected");
        assertNotNull(observer.getError(), "Expected a gRPC error");

        Throwable error = observer.getError();
        assertInstanceOf(StatusRuntimeException.class, error);

        StatusRuntimeException statusEx = (StatusRuntimeException) error;
        assertEquals(Status.INVALID_ARGUMENT.getCode(), statusEx.getStatus().getCode());
        assertTrue(statusEx.getStatus().getDescription().contains("Service name cannot be empty."));
    }
}
