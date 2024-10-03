package edu.cwru.ooa21.taskscheduler.models;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceMonitor {
    private final AtomicLong totalExecutionTime = new AtomicLong();
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger failureCount = new AtomicInteger();
    private final AtomicInteger taskCount = new AtomicInteger();

    public void recordTaskExecution(long executionTime, boolean success)
    {
        totalExecutionTime.addAndGet(executionTime);
        taskCount.incrementAndGet();
        if (success) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }
    }

    public double getAverageExecutionTime()
    {
        return taskCount.get() == 0 ? 0 : (double) totalExecutionTime.get() / taskCount.get();
    }

    public double getSuccessRate()
    {
        return taskCount.get() == 0 ? 0 : (double) successCount.get() / taskCount.get();
    }

    public double getFailureRate()
    {
        return taskCount.get() == 0 ? 0 : (double) failureCount.get() / taskCount.get();
    }

    public void logPerformanceMetrics()
    {
        System.out.println("Average Execution Time: " + getAverageExecutionTime());
        System.out.println("Success Rate: " + getSuccessRate());
        System.out.println("Failure Rate: " + getFailureRate());
    }
}
