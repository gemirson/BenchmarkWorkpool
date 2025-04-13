package com.example.workpool;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkpoolTest {
    @Test
    public void testWorkpoolCreation() {
        Workpool workpool = new EventLoopWorkpool(5, 100, 1000L);
        assertNotNull(workpool, "Workpool should be created");
    }

    @Test
    public void testTaskExecution() throws Exception {
        Workpool workpool = new EventLoopWorkpool(3, 100, 1000L);
        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        workpool.submitTask(() -> taskExecuted.set(true));
        Thread.sleep(100); // Allow time for task execution
        assertTrue(taskExecuted.get(), "Task should be executed");
    }
}
