package com.example.workpool;

import java.util.concurrent.RejectedExecutionException;

public interface Workpool {
    void submitTask(Runnable task) throws RejectedExecutionException;
    void shutdown();
}
