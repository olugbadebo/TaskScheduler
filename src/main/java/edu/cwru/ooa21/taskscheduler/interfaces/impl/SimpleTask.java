package edu.cwru.ooa21.taskscheduler.interfaces.impl;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.models.Duration;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SimpleTask implements Task, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String id;
    private final Duration estimatedDuration;
    private boolean isCompleted;
    private final TaskPriority priority;
    private final Set<String> dependencies;
    private final Duration timeOut;

    public SimpleTask(String id, Duration estimatedDuration, Duration timeOut) {
        this.timeOut = timeOut;
        this.id = id;
        this.estimatedDuration = estimatedDuration;
        this.isCompleted = false;
        this.priority = TaskPriority.LOW;
        this.dependencies = new HashSet<>();
    }

    public SimpleTask(SimpleTask copy) {
        this.id = copy.id;
        this.estimatedDuration = copy.estimatedDuration;
        this.isCompleted = copy.isCompleted;
        this.priority = copy.priority;
        this.timeOut = copy.timeOut;
        this.dependencies = new HashSet<>(copy.dependencies);
    }


    @Override
    public String getId() {
        // fetches the string ID
        return this.id;
    }

    @Override
    public boolean isCompleted() {

        // checks whether the task has been completed
        return this.isCompleted;
    }

    @Override
    public Duration getEstimatedDuration() {
        // returns the estimated duration of the task
        return this.estimatedDuration;
    }

    @Override
    public void execute() throws TaskException {
        if (isCompleted) {
            // an exception is thrown when the task in question is already completed
            throw new TaskException("This task has already been completed");
        }
        long startTime = System.currentTimeMillis(),
            timeout = this.getTimeOut().toMillis().longValue();
        try {
            // simulates the execution of the task by sleeping for the allotted time
            Thread.sleep(Math.min(this.getEstimatedDuration().toMillis().longValue(), timeout));
            isCompleted = true;

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > timeout) {
                throw new TaskException("Task execution timed out after " + timeout + " milliseconds");
            }
        } catch (InterruptedException e) {
            // throws an exception if the task was interrupted while sleeping/"executing" and wraps it as a task exception
            throw new TaskException("The task was interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            // throws an exception for any other failure during task execution
            throw new TaskException("Error during task execution: " + e.getMessage(), e);
        }
    }

    @Override
    public Task copy() {
        return new SimpleTask(this);
    }

    @Override
    public Duration getTimeOut() {
        return this.timeOut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTask that = (SimpleTask) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void cleanup() {
        System.out.println("Cleaning up resources for task: " + id);
    }


}
