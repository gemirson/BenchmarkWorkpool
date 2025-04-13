package com.example.workpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkpoolMain {
    private static final Logger logger = LoggerFactory.getLogger(WorkpoolMain.class);
    
    public static void main(String[] args) {
        // Create workpool with default configuration
        Workpool workpool = WorkpoolFactory.createDefaultWorkpool();
        
        // Submit 100 sample tasks
        for (int i = 0; i < 100; i++) {
            final int taskId = i;
            workpool.submitTask(() -> {
                logger.info("Starting task {}", taskId);
                
                // Simulate work
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                logger.info("Completed task {}", taskId);
            });
        }
        
        // Gracefully shutdown
        workpool.shutdown();
        logger.info("Workpool shutdown completed");
    }
}
