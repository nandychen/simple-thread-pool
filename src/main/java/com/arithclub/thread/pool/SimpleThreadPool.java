package com.arithclub.thread.pool;

import com.arithclub.thread.bean.TaskResult;
import com.arithclub.thread.task.WorkerThread;

import java.util.concurrent.*;

/**
 * 简单线程池
 */
public class SimpleThreadPool {

    public static void main(String[] args) {
        //建了包含5个工作线程的固定大小线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);
        //向线程池提交10个任务
        //由于线程池的大小是5，因此首先会启动5个工作线程，其他任务将进行等待。
        // 一旦有任务结束，工作线程会从等待队列中挑选下一个任务并开始执行。
        for (int i = 0; i < 10; i++) {
            Callable<TaskResult> worker = new WorkerThread(i, "command->" + i);
            Future<TaskResult> futureTask = executor.submit(worker);
            try {
                TaskResult result = futureTask.get();
                System.out.println("task result: " + result);
            } catch (InterruptedException e) {
                System.out.println("interrupted exception: " + e.getMessage());
            } catch (ExecutionException e) {
                System.out.println("execution exception: " + e.getMessage());
            }
        }

        executor.shutdown();
        long start = System.nanoTime();
        while (!executor.isTerminated()) {
            System.out.println("terminating thread pool...");
        }
        System.out.println("Finished all threads, cost time: " + (System.nanoTime() - start) / 1000000 + " ms");
    }

}
