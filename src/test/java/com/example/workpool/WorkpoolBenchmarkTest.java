package com.example.workpool;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

public class WorkpoolBenchmarkTest {
    private static final int WARMUP_ITERATIONS = 5;
    private static final int MEASUREMENT_ITERATIONS = 10;
    private static final int[] THREAD_COUNTS = {50, 100, 200, 500};
    private static final int[] TASK_COUNTS = {100_000, 1_000_000, 5_000_000};
    
    @Test
    public void runComprehensiveBenchmark() throws Exception {
        System.out.println("Starting comprehensive benchmark...\n");
        
        for (int threads : THREAD_COUNTS) {
            for (int tasks : TASK_COUNTS) {
                System.out.printf("=== Config: %d threads, %d tasks ===%n", threads, tasks);
                
                // Test SimpleWorkpool
                benchmarkWorkpool(new SimpleWorkpool(threads, tasks/2), 
                                "SimpleWorkpool", threads, tasks);
                
                // Test EventLoopWorkpool
                benchmarkWorkpool(new EventLoopWorkpool(threads, tasks/2, 10_000), 
                                "EventLoopWorkpool", threads, tasks);
                
                System.out.println();
            }
        }
    }

    private void benchmarkWorkpool(Workpool workpool, String name, 
                                 int threads, int totalTasks) throws Exception {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runTest(workpool, threads, totalTasks/10);
        }
        
        // Measurement
        long totalTime = 0;
        long totalTasksProcessed = 0;
        
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            TestResult result = runTest(workpool, threads, totalTasks);
            totalTime += result.duration;
            totalTasksProcessed += result.tasksCompleted;
        }
        
        // Calculate averages
        double avgThroughput = totalTasksProcessed / (totalTime / 1_000_000_000.0);
        double avgLatency = (totalTime / 1_000_000.0) / totalTasksProcessed;
        
        System.out.printf("%s Results:%n", name);
        System.out.printf("  Avg Throughput: %,.2f TPS%n", avgThroughput);
        System.out.printf("  Avg Latency: %.3f ms/task%n", avgLatency);
        System.out.printf("  Memory Usage: %d MB%n", 
                        (Runtime.getRuntime().totalMemory() - 
                         Runtime.getRuntime().freeMemory()) / (1024 * 1024));
    }

    private TestResult runTest(Workpool workpool, int threads, int taskCount) 
            throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        AtomicLong completedTasks = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(threads);
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < taskCount/threads; j++) {
                    workpool.submitTask(() -> {
                        completedTasks.incrementAndGet();
                    });
                }
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        workpool.shutdown();
        
        long duration = System.nanoTime() - startTime;
        return new TestResult(completedTasks.get(), duration);
    }
    
    private static class TestResult {
        final long tasksCompleted;
        final long duration;
        
        TestResult(long tasksCompleted, long duration) {
            this.tasksCompleted = tasksCompleted;
            this.duration = duration;
        }
    }
}
