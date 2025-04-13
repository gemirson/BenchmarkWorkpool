package com.example.workpool;

@FunctionalInterface
public interface Task {
    void execute() throws Exception;
    
    default Runnable asRunnable() {
        return () -> {
            try {
                execute();
            } catch (Exception e) {
                throw new WorkpoolException("Task execution failed", e);
            }
        };
    }
}
