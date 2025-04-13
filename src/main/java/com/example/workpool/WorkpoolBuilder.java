package com.example.workpool;

public class WorkpoolBuilder {
    private int maxThreads = Runtime.getRuntime().availableProcessors();
    private int queueSize = 1000;
    private long taskTimeoutMs = 100;

    public WorkpoolBuilder setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public WorkpoolBuilder setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public WorkpoolBuilder setTaskTimeoutMs(long taskTimeoutMs) {
        this.taskTimeoutMs = taskTimeoutMs;
        return this;
    }

    public Workpool build() {
        return new EventLoopWorkpool(maxThreads, queueSize, taskTimeoutMs);
    }
}
