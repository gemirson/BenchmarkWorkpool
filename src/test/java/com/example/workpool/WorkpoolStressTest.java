package com.example.workpool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

public class WorkpoolStressTest {
    private static final int THREAD_COUNT = 550;
    private static final int TASK_COUNT = 5_000_000;
    private static final int QUEUE_SIZE = 1_000_000;

    @Test
    @Timeout(300)
    public void testExtremeLoad() throws InterruptedException {
        Workpool workpool = new SimpleWorkpool(THREAD_COUNT, QUEUE_SIZE);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicLong completedTasks = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long startTime = System.nanoTime();

        // Submit tasks from multiple threads
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < TASK_COUNT/THREAD_COUNT; j++) {
                    workpool.submitTask(() -> {
                        // Simple task that does minimal work
                        Thread.yield();
                        completedTasks.incrementAndGet();
                    });
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        
        double duration = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double actualTPS = TASK_COUNT / duration;
        
        System.out.printf("\nStress Test Results:\n");
        System.out.printf("Tasks completed: %,d\n", completedTasks.get());
        System.out.printf("Throughput: %,.2f TPS\n", actualTPS);
        System.out.printf("Execution time: %.2f seconds\n", duration);
        
        assertTrue(completedTasks.get() >= TASK_COUNT * 0.999, "Should complete at least 99.9% of tasks");
        assertTrue(actualTPS >= 950_000, "Should maintain at least 950K TPS under stress");
    }
}
