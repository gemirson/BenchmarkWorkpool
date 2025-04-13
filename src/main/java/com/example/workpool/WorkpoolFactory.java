package com.example.workpool;

public class WorkpoolFactory {
    public enum WorkpoolType {
        EVENT_LOOP,
        SIMPLE
    }

    // Default configuration
    private static final int DEFAULT_QUEUE_SIZE = 10_000;
    private static final long DEFAULT_TIMEOUT_MS = 10_000;
    private static final WorkpoolType DEFAULT_TYPE = WorkpoolType.EVENT_LOOP;

    public static Workpool createDefaultWorkpool() {
        return createWorkpool(
            Runtime.getRuntime().availableProcessors(),
            DEFAULT_QUEUE_SIZE,
            DEFAULT_TIMEOUT_MS,
            DEFAULT_TYPE
        );
    }
    
    public static Workpool createCustomWorkpool(int maxThreads, int queueSize, long taskTimeoutMs) {
        return new WorkpoolBuilder()
            .setMaxThreads(maxThreads)
            .setQueueSize(queueSize)
            .setTaskTimeoutMs(taskTimeoutMs)
            .build();
    }

    public static Workpool createWorkpool(int maxThreads) {
        return createWorkpool(
            maxThreads,
            DEFAULT_QUEUE_SIZE,
            DEFAULT_TIMEOUT_MS,
            DEFAULT_TYPE
        );
    }

    public static Workpool createWorkpool(int maxThreads, int queueSize, long taskTimeoutMs, WorkpoolType type) {
        return type == WorkpoolType.SIMPLE 
            ? new SimpleWorkpool(maxThreads, queueSize)
            : new EventLoopWorkpool(maxThreads, queueSize, taskTimeoutMs);
    }
}
