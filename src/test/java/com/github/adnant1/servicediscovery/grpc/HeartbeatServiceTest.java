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
import com.github.adnant1.servicediscovery.registry.HeartbeatRequest;
import com.github.adnant1.servicediscovery.registry.HeartbeatResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class HeartbeatServiceTest {
    
    private RedisRepository redisRepository;
    private RegistryServiceImpl service;

    @BeforeEach
    public void setUp() {
        this.redisRepository = Mockito.mock(RedisRepository.class);
        this.service = new RegistryServiceImpl(redisRepository);
    }

    @Test
    void testHeartbeatSuccess() {
        // Arrange
        Mockito.when(redisRepository.refreshTtl("auth-service", "instance-1"))
                .thenReturn(true);

        HeartbeatRequest request = HeartbeatRequest.newBuilder()
                .setServiceName("auth-service")
                .setInstanceId("instance-1")
                .build();

        TestStreamObserver<HeartbeatResponse> observer = new TestStreamObserver<>();

        // Act
        service.heartbeat(request, observer);

        // Assert
        assertNull(observer.getError());
        assertTrue(observer.isCompleted());

        HeartbeatResponse response = observer.getResponse();
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Heartbeat received, TTL refreshed.", response.getMessage());
    }

    @Test
    void testHeartbeatInstanceNotFound() {
        // Arrange
        Mockito.when(redisRepository.refreshTtl("auth-service", "missing-instance"))
                .thenReturn(false);

        HeartbeatRequest request = HeartbeatRequest.newBuilder()
                .setServiceName("auth-service")
                .setInstanceId("missing-instance")
                .build();

        TestStreamObserver<HeartbeatResponse> observer = new TestStreamObserver<>();

        // Act
        service.heartbeat(request, observer);

        // Assert
        assertNull(observer.getError());
        assertTrue(observer.isCompleted());

        HeartbeatResponse response = observer.getResponse();
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Service instance not found.", response.getMessage());
    }

    @Test
    void testHeartbeatInvalidServiceName() {
        // Arrange
        HeartbeatRequest request = HeartbeatRequest.newBuilder()
                .setServiceName("") // invalid
                .setInstanceId("instance-1")
                .build();

        TestStreamObserver<HeartbeatResponse> observer = new TestStreamObserver<>();

        // Act
        service.heartbeat(request, observer);

        // Assert
        assertNull(observer.getResponse());
        assertNotNull(observer.getError());

        Throwable error = observer.getError();
        assertInstanceOf(StatusRuntimeException.class, error);

        StatusRuntimeException statusEx = (StatusRuntimeException) error;
        assertEquals(Status.INVALID_ARGUMENT.getCode(), statusEx.getStatus().getCode());
        assertTrue(statusEx.getStatus().getDescription().contains("Service name cannot be empty."));
    }

    @Test
    void testHeartbeatInvalidInstanceId() {
        // Arrange
        HeartbeatRequest request = HeartbeatRequest.newBuilder()
                .setServiceName("auth-service")
                .setInstanceId("") // invalid
                .build();

        TestStreamObserver<HeartbeatResponse> observer = new TestStreamObserver<>();

        // Act
        service.heartbeat(request, observer);

        // Assert
        assertNull(observer.getResponse());
        assertNotNull(observer.getError());

        Throwable error = observer.getError();
        assertInstanceOf(StatusRuntimeException.class, error);

        StatusRuntimeException statusEx = (StatusRuntimeException) error;
        assertEquals(Status.INVALID_ARGUMENT.getCode(), statusEx.getStatus().getCode());
        assertTrue(statusEx.getStatus().getDescription().contains("Instance ID cannot be empty."));
    }
}
