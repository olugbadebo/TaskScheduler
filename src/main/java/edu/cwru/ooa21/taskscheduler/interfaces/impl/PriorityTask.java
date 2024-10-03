package edu.cwru.ooa21.taskscheduler.interfaces.impl;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.models.Duration;

import java.io.Serializable;

public class PriorityTask extends SimpleTask implements Serializable {
    private final TaskPriority priority;

    public PriorityTask(String id, Duration estimatedDuration, TaskPriority priority, Duration timeOut) {
        super(id, estimatedDuration, timeOut);
        this.priority = priority;
    }

    public TaskPriority getPriority() {
        return this.priority;
    }

    @Override
    public Task copy() {
        return new PriorityTask(this.getId(), this.getEstimatedDuration(), this.priority, this.getTimeOut());
    }

    @Override
    public void cleanup() {
        System.out.println("Cleaning up resources for task: " + this.getId());
    }
}
