package com.example.workpool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Queue<T> pool;
    private final Supplier<T> objectFactory;

    public ObjectPool(int size, Supplier<T> objectFactory) {
        this.pool = new ConcurrentLinkedQueue<>();
        this.objectFactory = objectFactory;
        for (int i = 0; i < size; i++) {
            pool.add(objectFactory.get());
        }
    }

    public T borrowObject() {
        T obj = pool.poll();
        return obj != null ? obj : objectFactory.get();
    }

    public void returnObject(T obj) {
        pool.offer(obj);
    }
}
