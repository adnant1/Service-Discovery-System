package com.github.adnant1.servicediscovery;

import io.grpc.stub.StreamObserver;

/**
 * A simple implementation of StreamObserver for testing purposes.
 */
public class TestStreamObserver<T> implements StreamObserver<T> {
    private T response;
    private Throwable error;
    private boolean completed = false;

    @Override
    public void onNext(T value) {
        this.response = value;
    }

    @Override
    public void onError(Throwable t) {
        this.error = t;
    }

    @Override
    public void onCompleted() {
        this.completed = true;
    }

    public T getResponse() {
        return response;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isCompleted() {
        return completed;
    }
    
}
