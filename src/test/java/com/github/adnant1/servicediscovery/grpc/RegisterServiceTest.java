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
import com.github.adnant1.servicediscovery.registry.RegisterRequest;
import com.github.adnant1.servicediscovery.registry.RegisterResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;


/**
 * Unit tests for the Register gRPC method in RegistryServiceImpl.
 */
public class RegisterServiceTest {
    
    private RedisRepository redisRepository;
    private RegistryServiceImpl service;

    @BeforeEach
    public void setUp() {
        this.redisRepository = Mockito.mock(RedisRepository.class);
        this.service = new RegistryServiceImpl(redisRepository);
    }

    @Test
    void testRegisterValidInstance() {
        // Create
        RegisterRequest request = RegisterRequest.newBuilder()
                .setServiceName("auth-service")
                .setInstanceId("instance-1")
                .setIp("10.0.0.1")
                .setPort(8080)
                .build();
        
        Mockito.doNothing().when(redisRepository).saveInstance(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyInt()
        );

        TestStreamObserver<RegisterResponse> observer = new TestStreamObserver<>();

        // Act
        service.register(request, observer);

        // Assert
        assertNull(observer.getError(), "No error should occur");
        assertTrue(observer.isCompleted(), "Stream should be completed");

        RegisterResponse response = observer.getResponse();
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Service registered successfully.", response.getMessage());
    }

    @Test
    void testRegisterMissingServiceName() {
        // Create
        RegisterRequest request = RegisterRequest.newBuilder()
                .setServiceName("")
                .setInstanceId("instance-1")
                .setIp("10.0.0.1")
                .setPort(8080)
                .build();
        
        TestStreamObserver<RegisterResponse> observer = new TestStreamObserver<>();

        // Act
        service.register(request, observer);

        // Assert
        // Assert
        assertNull(observer.getResponse(), "No response expected on invalid input");
        assertNotNull(observer.getError(), "Expected a gRPC error");

        Throwable error = observer.getError();
        assertInstanceOf(StatusRuntimeException.class, error);

        StatusRuntimeException statusEx = (StatusRuntimeException) error;
        assertEquals(Status.INVALID_ARGUMENT.getCode(), statusEx.getStatus().getCode());
        assertTrue(statusEx.getStatus().getDescription().contains("Service name cannot be empty."));
    }
}
