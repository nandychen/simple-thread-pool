package com.arithclub.thread.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * A class that calculates the optimal thread pool boundaries. It takes the
 * desired target utilization and the desired work queue memory consumption as
 * input and retuns thread count and work queue capacity.
 *
 * @author Niklas Schlimm
 */
public abstract class PoolSizeCalculator {

    /**
     * The sample queue size to calculate the size of a single {@link Runnable}
     * element.
     */
    private final int SAMPLE_QUEUE_SIZE = 1000;

    /**
     * Accuracy of test run. It must finish within 20ms of the testTime
     * otherwise we retry the test. This could be configurable.
     */
    private final int EPSYLON = 100;

    /**
     * Control variable for the CPU time investigation.
     */
    private volatile boolean expired;

    /**
     * Time (millis) of the test run in the CPU time calculation.
     */
    private final long testTime = 3000;

    /**
     * Calculates the boundaries of a thread pool for a given {@link Runnable}.
     *
     * @param targetUtilization    the desired utilization of the CPUs (0 <= targetUtilization <=
     *                             1)
     * @param targetQueueSizeBytes the desired maximum work queue size of the thread pool (bytes)
     */
    protected void calculateBoundaries(BigDecimal targetUtilization,
                                       BigDecimal targetQueueSizeBytes) {
        calculateOptimalCapacity(targetQueueSizeBytes);
        Runnable task = createTask();
        start(task);
        start(task); // warm up phase
        long cpuTime = getCurrentThreadCPUTime();
        start(task); // test interval
        cpuTime = getCurrentThreadCPUTime() - cpuTime;
        long waitTime = (testTime * 1000000) - cpuTime;
        calculateOptimalThreadCount(cpuTime, waitTime, targetUtilization);
    }

    private void calculateOptimalCapacity(BigDecimal targetQueueSizeBytes) {
        long mem = calculateMemoryUsage();
        BigDecimal queueCapacity = targetQueueSizeBytes.divide(new BigDecimal(
                mem), RoundingMode.HALF_UP);
        System.out.println("Target queue memory usage (bytes): "
                + targetQueueSizeBytes);
        System.out.println("createTask() produced "
                + createTask().getClass().getName() + " which took " + mem
                + " bytes in a queue");
        System.out.println("Formula: " + targetQueueSizeBytes + " / " + mem);
        System.out.println("* Recommended queue capacity (bytes): "
                + queueCapacity);
    }

    /**
     * * Brian Goetz' optimal thread count formula, see 'Java Concurrency in *
     * Practice' (chapter 8.2) * * @param cpu * cpu time consumed by considered
     * task * @param wait * wait time of considered task * @param
     * targetUtilization * target utilization of the system
     */
    private void calculateOptimalThreadCount(long cpu, long wait,
                                             BigDecimal targetUtilization) {
        BigDecimal waitTime = new BigDecimal(wait);
        BigDecimal computeTime = new BigDecimal(cpu);
        BigDecimal numberOfCPU = new BigDecimal(Runtime.getRuntime()
                .availableProcessors());
        BigDecimal optimalThreadCount = numberOfCPU.multiply(targetUtilization)
                .multiply(
                        new BigDecimal(1).add(waitTime.divide(computeTime,
                                RoundingMode.HALF_UP)));
        System.out.println("Number of CPU: " + numberOfCPU);
        System.out.println("Target utilization: " + targetUtilization);
        System.out.println("Elapsed time (nanos): " + (testTime * 1000000));
        System.out.println("Compute time (nanos): " + cpu);
        System.out.println("Wait time (nanos): " + wait);
        System.out.println("Formula: " + numberOfCPU + " * "
                + targetUtilization + " * (1 + " + waitTime + " / "
                + computeTime + ")");
        System.out.println("* Optimal thread count: " + optimalThreadCount);
    }

    /**
     * * Runs the {@link Runnable} over a period defined in {@link #testTime}. *
     * Based on Heinz Kabbutz' ideas *
     * (http://www.javaspecialists.eu/archive/Issue124.html). * * @param task *
     * the runnable under investigation
     */
    public void start(Runnable task) {
        long start = 0;
        int runs = 0;
        do {
            if (++runs > 5) {
                throw new IllegalStateException("Test not accurate");
            }
            expired = false;
            start = System.currentTimeMillis();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    expired = true;
                    System.out.println("timer schedule finished......");
                }
            }, testTime);
            while (!expired) {
                task.run();
                System.out.println("task running......");
            }
            start = System.currentTimeMillis() - start;
            System.out.println("task cost time: " + start);
            timer.cancel();
        } while (Math.abs(start - testTime) > EPSYLON);
        collectGarbage(3);
    }

    /**
     * 强制垃圾回收
     *
     * @param times 循环次数
     */
    private void collectGarbage(int times) {
        for (int i = 0; i < times; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Calculates the memory usage of a single element in a work queue. Based on
     * Heinz Kabbutz' ideas
     * (http://www.javaspecialists.eu/archive/Issue029.html).
     *
     * @return memory usage of a single {@link Runnable} element in the thread
     * pools work queue
     */
    public long calculateMemoryUsage() {
        BlockingQueue<Runnable> queue = createWorkQueue();
        for (int i = 0; i < SAMPLE_QUEUE_SIZE; i++) {
            queue.add(createTask());
        }
        long mem0 = usedMemory();
        long mem1 = usedMemory();

        queue = null;
        collectGarbage(15);

        mem0 = usedMemory();
        queue = createWorkQueue();
        for (int i = 0; i < SAMPLE_QUEUE_SIZE; i++) {
            queue.add(createTask());
        }
        collectGarbage(15);
        mem1 = usedMemory();
        return (mem1 - mem0) / SAMPLE_QUEUE_SIZE;
    }

    /**
     * 已用内存
     *
     * @return long (measured in bytes)
     */
    private long usedMemory() {
        return Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();
    }

    /**
     * Create your runnable task here.
     *
     * @return an instance of your runnable task under investigation
     */
    protected abstract Runnable createTask();

    /**
     * Return an instance of the queue used in the thread pool.
     *
     * @return queue instance
     */
    protected abstract BlockingQueue<Runnable> createWorkQueue();

    /**
     * Calculate current cpu time. Various frameworks may be used here,
     * depending on the operating system in use. (e.g.
     * http://www.hyperic.com/products/sigar). The more accurate the CPU time
     * measurement, the more accurate the results for thread count boundaries.
     *
     * @return current cpu time of current thread
     */
    protected abstract long getCurrentThreadCPUTime();

}