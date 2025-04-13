package com.example.workpool;

public class PooledTask implements Runnable {
    private Runnable wrappedTask;

    public void setWrappedTask(Runnable task) {
        this.wrappedTask = task;
    }

    @Override
    public void run() {
        try {
            wrappedTask.run();
        } finally {
            wrappedTask = null;
        }
    }
}
