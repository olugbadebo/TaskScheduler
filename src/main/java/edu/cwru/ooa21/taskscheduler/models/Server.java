package edu.cwru.ooa21.taskscheduler.models;

import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.utils.AlertSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    private static final AlertSystem logger = new AlertSystem();
    private final List<Task> taskQueue;
    private Duration serverDuration;
    private final String serverId;

    private boolean circuitOpen = true;
    private int failureThreshold;
    private int attempts;

    public Server(String serverId, int failureThreshold) {
        this.serverId = serverId;
        this.taskQueue = new ArrayList<>();
        this.serverDuration = Duration.ofMillis(0);
        this.failureThreshold = failureThreshold;
    }

    public Server(Server copy) {
        this.serverId = copy.serverId;
        this.serverDuration = (copy.serverDuration != null) ? copy.serverDuration : Duration.ofMillis(0);
        this.taskQueue = (copy.taskQueue != null)
            ? copy.taskQueue.stream().map(Task::copy).collect(Collectors.toList())
            : new ArrayList<>();
    }

    public boolean isCircuitOpen() {
        return circuitOpen;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }


    public synchronized void addTask(Task task1) {
        if (task1 == null) {
            logger.warn("Attempted to add a null task.");
            throw new IllegalArgumentException("Task cannot be null");
        }
        taskQueue.add(task1);
        logger.info("Added task " + task1.getId() + " to server " + serverId);
        serverDuration = serverDuration.add(task1.getEstimatedDuration());
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(taskQueue);
    }

    public String getServerId() {
        return this.serverId;
    }

    public int getTaskCount() {
        return taskQueue.size();
    }

    public List<Task> executeTasks() throws TaskException {
        PerformanceMonitor performanceMonitor = new PerformanceMonitor();
        try {
            List<Task> completedTasks = new ArrayList<>();

            for (Task task : taskQueue) {
                if (!task.isCompleted()) {
                    long startTime = System.nanoTime();
                    boolean success = false;
                    try {
                        if (task.getTimeOut() != null) {
                            long remainingTime = task.getTimeOut().toMillis().longValue();

                            Thread taskThread = new Thread(() -> {
                                try {
                                    task.execute();
                                } catch (TaskException e) {
                                    logger.error("Task %s failed to execute".formatted(task.getId()));
                                }
                            });

                            taskThread.start();

                            while (taskThread.isAlive() && remainingTime > 0) {
                                remainingTime -= 100;
                                Thread.sleep(100);
                            }

                            if (taskThread.isAlive()) {
                                taskThread.interrupt();
                                throw new TaskException("Task %s timed out.".formatted(task.getId()));
                            }

                        } else {
                            Thread taskThread = new Thread(() -> {
                                try {
                                    task.execute();
                                } catch (TaskException e) {
                                    logger.error("Task %s failed to execute".formatted(task.getId()));
                                }
                            });

                            taskThread.start();

                        }
                        completedTasks.add(task);
                        logger.info("Task " + task.getId() + " executed successfully.");
                    } catch (TaskException e) {
                        logger.error(e.getMessage());
                        task.cleanup();
                        throw e;
                    }
                    finally {
                        long endTime = System.nanoTime();
                        long executionTime = endTime - startTime;
                        performanceMonitor.recordTaskExecution(executionTime / 1_000_000, success);
                    }
                }
            }

            taskQueue.removeAll(completedTasks);
            return completedTasks;
        } catch (InterruptedException | TaskException e) {
            logger.error("Error executing tasks: " + e.getMessage());
            throw new TaskException("Error executing tasks: " + e.getMessage());
        }
    }

    public List<Task> getFailedTasks() {
        return taskQueue.stream().filter(task -> !task.isCompleted()).collect(Collectors.toList());
    }

    public Duration getServerDuration() {
        return serverDuration;
    }
}
