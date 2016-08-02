package com.arithclub.thread.pool;

import com.arithclub.thread.admin.MyMonitorThread;
import com.arithclub.thread.bean.TaskResult;
import com.arithclub.thread.handler.RejectedExecutionHandlerImpl;
import com.arithclub.thread.task.WorkerThread;

import java.util.concurrent.*;

/**
 * 自定义线程池
 */
public class CustomThreadPool {

    public static void main(String args[]) throws InterruptedException {
        //自定义线程池饱和策略
        RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
        //默认的线程工厂实现
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        //定制线程池
        //初始线程池大小设为2、最大值设为4、工作队列大小设为2
        //所以，如果当前有4个任务正在运行而此时又有新任务提交，
        //工作队列将只存储2个任务，其他任务将交由RejectedExecutionHandlerImpl处理
        ThreadPoolExecutor executorPool
                = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2), threadFactory, rejectionHandler);

        //启动线程池监控
        MyMonitorThread monitor = new MyMonitorThread(executorPool, 3);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        //CompletionService可以实现，哪个任务先执行完成就返回，
        //而不是按顺序返回，这样可以极大的提升效率
        CompletionService<TaskResult> completionService
                = new ExecutorCompletionService<>(executorPool, new LinkedBlockingQueue<Future<TaskResult>>(5));
        //向线程池提交任务
        for (int i = 0; i < 10; i++) {
            completionService.submit(new WorkerThread(i, "command:" + i));
        }

        for (int i = 0; i < 10; i++) {
            try {
                TaskResult result = completionService.take().get();
                System.out.println("completion service take: " + result);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        Thread.sleep(30000);
        //关闭线程池
        executorPool.shutdown();
        Thread.sleep(5000);
        //关闭线程池监控
        monitor.shutdown();
    }

}
