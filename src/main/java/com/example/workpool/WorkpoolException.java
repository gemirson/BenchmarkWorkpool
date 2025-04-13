package com.example.workpool;

public class WorkpoolException extends RuntimeException {
    public WorkpoolException(String message) {
        super(message);
    }

    public WorkpoolException(String message, Throwable cause) {
        super(message, cause);
    }
}
