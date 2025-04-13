package com.example.workpool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLoopWorkpool implements Workpool {
    private static final Logger logger = LoggerFactory.getLogger(EventLoopWorkpool.class);
    private final ExecutorService executor;
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final int maxThreads;
    private final long taskTimeoutNanos;
    private final BlockingQueue<Runnable> taskQueue;
    private final ObjectPool<Runnable> taskPool;

    public EventLoopWorkpool(int maxThreads, int queueSize, long taskTimeoutMs) {
        this.maxThreads = maxThreads;
        this.taskTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(taskTimeoutMs);
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        // Size pool to expected concurrent tasks rather than max threads
        this.taskPool = new ObjectPool<>(Math.min(1000, maxThreads), () -> new PooledTask());
        
        ThreadFactory factory = Thread.ofVirtual().factory();
        this.executor = Executors.newThreadPerTaskExecutor(factory);
    }

    public void submitTask(Runnable task) throws RejectedExecutionException {
        PooledTask pooledTask = (PooledTask)taskPool.borrowObject();
        pooledTask.setWrappedTask(task);
        if (!taskQueue.offer(pooledTask)) {
            taskPool.returnObject(pooledTask);
            throw new RejectedExecutionException("Workpool queue is full");
        }
        
        if (activeTasks.get() < maxThreads) {
            activeTasks.incrementAndGet();
            executor.submit(this::workerLoop);
        }
    }

    private void workerLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                PooledTask task = (PooledTask)taskQueue.poll();
                if (task != null) {
                    try {
                        task.run();
                        taskPool.returnObject(task);
                    } catch (Throwable t) {
                        logger.error("Task execution failed", t);
                        taskPool.returnObject(task);
                    }
                    continue;
                }

                if (activeTasks.get() > maxThreads) {
                    break;
                }
                LockSupport.parkNanos(taskTimeoutNanos);
            }
        } finally {
            activeTasks.decrementAndGet();
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
