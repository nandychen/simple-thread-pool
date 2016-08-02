package com.arithclub.thread.task;

import com.arithclub.thread.bean.TaskResult;

import java.util.concurrent.Callable;

/**
 * 工作线程
 */
public class WorkerThread implements Callable<TaskResult> {

    private int key;
    private String command;

    public WorkerThread(int key, String s) {
        this.key = key;
        this.command = s;
    }

    @Override
    public TaskResult call() throws Exception {
        System.out.println(Thread.currentThread().getName() + " Start, Command = " + command);
        processCommand();
        System.out.println(Thread.currentThread().getName() + " End");

        TaskResult tr = new TaskResult();
        tr.setKey(this.key);
        tr.setResult(this.command);
        return tr;
    }

    //模拟具体耗时任务
    private void processCommand() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return this.key + ": " + this.command;
    }

}
