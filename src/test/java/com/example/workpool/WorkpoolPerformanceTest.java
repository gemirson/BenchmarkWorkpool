package com.example.workpool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkpoolPerformanceTest {
    private static final int HIGH_TPS_THREAD_COUNT = 32;
    private static final int HIGH_TPS_TASK_COUNT = 10_000;

    @Test
    @Timeout(120)
    public void test10000TPSWorkload() throws InterruptedException {
        Workpool workpool = new EventLoopWorkpool(HIGH_TPS_THREAD_COUNT, 100_000, 10_000L);
        ExecutorService executor = Executors.newFixedThreadPool(HIGH_TPS_THREAD_COUNT);
        AtomicInteger completedTasks = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(HIGH_TPS_TASK_COUNT / 1000);
        
        long startTime = System.nanoTime();

        for (int i = 0; i < HIGH_TPS_TASK_COUNT / 1000; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    workpool.submitTask(() -> {
                        Thread.yield();
                    });
                    completedTasks.incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        
        double duration = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double actualTPS = HIGH_TPS_TASK_COUNT / duration;
        
        assertEquals(HIGH_TPS_TASK_COUNT, completedTasks.get());
        assertTrue(actualTPS >= 5000, "Should achieve at least 5,000 TPS");
    }
}
