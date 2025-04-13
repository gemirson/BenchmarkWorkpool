package com.example.workpool;

import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleWorkpool implements Workpool {
    private static final Logger logger = LoggerFactory.getLogger(SimpleWorkpool.class);
    private final ThreadPoolExecutor executor;
    private final int maxThreads;

    public SimpleWorkpool(int maxThreads, int queueSize) {
        this.maxThreads = maxThreads;
        this.executor = new ThreadPoolExecutor(
            maxThreads,
            maxThreads,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(queueSize), // Using ArrayBlockingQueue now
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Override
    public void submitTask(Runnable task) throws RejectedExecutionException {
        try {
            executor.execute(task);
        } catch (RejectedExecutionException e) {
            logger.warn("Task rejected - queue: {}/{} ({}% full)", 
                      executor.getQueue().size(),
                      executor.getQueue().remainingCapacity(),
                      100 * executor.getQueue().size() / 
                      (executor.getQueue().size() + executor.getQueue().remainingCapacity()));
            
            // Retry logic with exponential backoff
            int retries = 0;
            while (retries < 3) {
                try {
                    Thread.sleep((long)Math.pow(2, retries) * 10); // 10, 20, 40ms
                    executor.execute(task);
                    return;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RejectedExecutionException("Interrupted during retry", ie);
                } catch (RejectedExecutionException ree) {
                    retries++;
                }
            }
            throw e;
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            // Wait longer for in-progress tasks
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                // Additional wait after shutdownNow
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int getQueueSize() {
        return executor.getQueue().size();
    }

    public int getActiveThreads() {
        return executor.getActiveCount();
    }
}
