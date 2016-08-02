package com.arithclub.thread.util;

import com.arithclub.thread.task.AsyncIOTask;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 线程池和队列大小估算工具
 */
public class SimplePoolSizeCalculator extends PoolSizeCalculator {

    public static void main(String[] args) {
        PoolSizeCalculator poolSizeCalculator = new SimplePoolSizeCalculator();
        poolSizeCalculator.calculateBoundaries(new BigDecimal(1.0),
                new BigDecimal(100000));
    }

    @Override
    protected Runnable createTask() {
        return new AsyncIOTask();
    }

    @Override
    protected BlockingQueue<Runnable> createWorkQueue() {
        return new LinkedBlockingQueue<Runnable>(1000);
    }

    @Override
    protected long getCurrentThreadCPUTime() {
        return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    }

}
