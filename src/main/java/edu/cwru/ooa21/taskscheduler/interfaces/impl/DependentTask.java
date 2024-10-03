package edu.cwru.ooa21.taskscheduler.interfaces.impl;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.models.Duration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class DependentTask extends PriorityTask implements Serializable {
    private final Set<String> dependencies;

    public DependentTask(String id, Duration estimatedDuration, TaskPriority priority, Duration timeOut, Set<String> dependencies) {
        super(id, estimatedDuration, priority, timeOut);
        this.dependencies = dependencies;
    }

    public Set<String> getDependencies() {
        return Collections.unmodifiableSet(this.dependencies);
    }

    @Override
    public Task copy() {
        return new DependentTask(this.getId(), this.getEstimatedDuration(), this.getPriority(), this.getTimeOut(), new HashSet<>(this.dependencies));
    }

    @Override
    public void cleanup() {
        System.out.println("Cleaning up resources for task: " + this.getId());
    }

}
