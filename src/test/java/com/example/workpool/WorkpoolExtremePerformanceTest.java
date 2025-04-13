package com.example.workpool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.invoke.VarHandle;

public class WorkpoolExtremePerformanceTest {
    private static final int MILLION_TPS_THREAD_COUNT =  Runtime.getRuntime().availableProcessors() * 2;
    private static final int MILLION_TPS_TASK_COUNT = 1_000_000;

    @Test
    @Timeout(600)
    public void test1MillionTPSWorkloadEventLoop() throws InterruptedException {
        runPerformanceTest(new EventLoopWorkpool(MILLION_TPS_TASK_COUNT, 100_000, 10_000L), "EventLoop");
    }

    @Test
    @Timeout(600)
    public void test1MillionTPSWorkloadSimple() throws InterruptedException {
        runPerformanceTest(new SimpleWorkpool(MILLION_TPS_THREAD_COUNT, 500_000), "Simple");
    }

    private void runPerformanceTest(Workpool workpool, String type) throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();
        
        ExecutorService executor = Executors.newFixedThreadPool(MILLION_TPS_THREAD_COUNT);
        AtomicLong completedTasks = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(MILLION_TPS_TASK_COUNT / 1000);
        
        long startTime = System.nanoTime();

        for (int i = 0; i < MILLION_TPS_TASK_COUNT / 1000; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    workpool.submitTask(() -> {
                        VarHandle.fullFence();
                        int dummy = Thread.currentThread().hashCode() % 256;
                    });
                    completedTasks.incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        
        double duration = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double actualTPS = MILLION_TPS_TASK_COUNT / duration;
        
        long endMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsedMB = (endMemory - startMemory) / (1024 * 1024);
        
        System.out.printf("\n%s Workpool Results:\n", type);
        System.out.printf("Memory used: %d MB\n", memoryUsedMB);
        System.out.printf("Throughput: %,.2f TPS\n", actualTPS);
        System.out.printf("Execution time: %.2f seconds\n", duration);
        
        assertEquals(MILLION_TPS_TASK_COUNT, completedTasks.get());
        assertTrue(actualTPS >= 120000, "Should achieve at least 120,000 TPS");
    }
}
