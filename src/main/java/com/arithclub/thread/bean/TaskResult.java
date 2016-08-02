package com.arithclub.thread.bean;

/**
 * 任务执行结果
 */
public class TaskResult {

    private int key;
    private String result;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "key=" + key +
                ", result='" + result + '\'' +
                '}';
    }

}
