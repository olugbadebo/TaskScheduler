package edu.cwru.ooa21.taskscheduler.schedulers;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.exceptions.SchedulerException;
import edu.cwru.ooa21.taskscheduler.exceptions.SchedulerFullException;
import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.DependentTask;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.PriorityTask;
import edu.cwru.ooa21.taskscheduler.models.RemoteServer;
import edu.cwru.ooa21.taskscheduler.models.Server;
import edu.cwru.ooa21.taskscheduler.policies.RetryPolicy;
import edu.cwru.ooa21.taskscheduler.utils.AlertSystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class TaskScheduler {
    private static final AlertSystem logger = new AlertSystem();
    private final PriorityBlockingQueue<DependentTask> taskQueue;
    private final Map<Server, List<String>> serverList;
    private final int capacity;
    private final Object lock = new Object();
    private final RetryPolicy retryPolicy;

    private List<String> circuitClosedServers = new ArrayList<>();

    public TaskScheduler(int capacity, RetryPolicy retryPolicy) {
        this.taskQueue = new PriorityBlockingQueue<>(capacity, Comparator.comparingInt(task -> task.getPriority().getPriority()));
        this.serverList = new HashMap<>();
        this.capacity = capacity;
        this.retryPolicy = retryPolicy;
    }

    public void addServer(Server server) {
        synchronized (lock) {
            Server serverCopy = new Server(server);

            serverList.put(serverCopy, new ArrayList<>());
        }
    }

    public void addServer(RemoteServer server) {
        synchronized (lock) {
            Server serverCopy = new Server(server);

            serverList.put(serverCopy, new ArrayList<>());
        }
    }

    private void retryTask(Task task, int attempt) throws SchedulerException {
        if (attempt > retryPolicy.getMaxRetries()) {
            logger.error("Task " + task.getId() + " failed after " + retryPolicy.getMaxRetries() + " attempts.");
            throw new SchedulerException("Exceeded max retries according to policy, attempts: %d.".formatted(attempt));
        }

        try {
            logger.debug("Retrying task " + task.getId() + " (Attempt " + attempt + ")");
            task.execute();
            logger.info("Task " + task.getId() + " succeeded on attempt " + attempt);
        } catch (TaskException e) {
            logger.error("Task execution failed (Attempt " + attempt + "): " + e.getMessage());
            long delay = retryPolicy.getDelayInMillis(attempt);
            logger.debug("Waiting " + delay + " milliseconds before retrying...");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                throw new SchedulerException("Retry interrupted", ie);
            }
            retryTask(task, attempt + 1);
        }
    }

    public void scheduleTask(List<Task> tasks) throws SchedulerFullException, SchedulerException {
        synchronized (lock) {
            for (Task task : tasks) {
                if (taskQueue.size() >= capacity) {
                    logger.warn("Scheduler is full, cannot schedule task " + task.getId());
                    throw new SchedulerFullException("Scheduler is full, new tasks cannot be scheduled.");
                }

                if (serverList.isEmpty()) {
                    logger.warn("Attempted to schedule task " + task.getId() + ". No servers are available.");
                    throw new SchedulerFullException("No servers available.");
                }

                if (!(task instanceof PriorityTask)) {
                    logger.info("Task " + task.getId() + " is a base task, casting to low priority task.");
                    task = new PriorityTask(task.getId(), task.getEstimatedDuration(), TaskPriority.LOW, task.getTimeOut());
                }

                if (!(task instanceof DependentTask)) {
                    logger.info("Task " + task.getId() + " is a priority task, casting to no dependency dependent task.");
                    task = new DependentTask(task.getId(), task.getEstimatedDuration(), ((PriorityTask) task).getPriority(), task.getTimeOut(), Set.of());
                }

                DependentTask dependentTask = (DependentTask) task;
                logger.info("Scheduling task %s with priority %s and %d dependencies".formatted(
                    dependentTask.getId(),
                    dependentTask.getPriority(),
                    dependentTask.getDependencies().size()
                ));

                taskQueue.add((DependentTask) task);
            }

            while (!taskQueue.isEmpty()) {
                Server server = findLeastLoadedServer();
                if (server != null) {
                    if (server.getAttempts() >= server.getFailureThreshold()) {
                        circuitClosedServers.add(server.getServerId());
                        logger.error("Server %s is down, closing its circuit".formatted(server.getServerId()));
                        continue;
                    }

                    if (server instanceof RemoteServer remoteServer) {
                        try (Socket clientSocket = new Socket("localhost", remoteServer.getPort());
                             ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                             ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {

                            outputStream.writeObject("ADD_TASK");
                            String response1 = (String) inputStream.readObject();
                            if (!response1.equals("Send Task")) {
                                remoteServer.setAttempts(remoteServer.getAttempts() + 1);
                            }

                            outputStream.writeObject(taskQueue.poll());
                            String response2 = (String) inputStream.readObject();
                            if (!response2.equals("Task added successfully.")) {
                                remoteServer.setAttempts(remoteServer.getAttempts() + 1);
                            }
                        } catch (UnknownHostException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    } else
                        server.addTask(taskQueue.poll());
                } else throw new SchedulerException("There are no available servers");
            }
        }
    }

    public LinkedHashMap<String, List<Task>> executeAll() throws SchedulerException {
        synchronized (lock) {
            if (serverList.isEmpty()) {
                logger.warn("No servers available to execute tasks.");
                throw new SchedulerException("No servers available to execute tasks.");
            }

            LinkedHashMap<String, List<Task>> executionMap = new LinkedHashMap<>();

            for (Server server : serverList.keySet()) {
                try {
                    List<Task> completedTasks = new ArrayList<>();
                    List<Task> prioritySortedTaskList = server.getTasks();
                    prioritySortedTaskList.sort(Comparator.comparingInt(task -> {
                        if (task instanceof PriorityTask dependentTask)
                            return dependentTask.getPriority().getPriority();

                        return TaskPriority.LOW.getPriority();
                    }));
                    prioritySortedTaskList = dependencySort(prioritySortedTaskList);

                    for (Task task : prioritySortedTaskList) {
                        if (!task.isCompleted()) {
                            try {
                                task.execute();
                                completedTasks.add(task);
                                logger.info("Executed tasks on server " + server.getServerId() + ": " + completedTasks.size() + " tasks completed.");
                            } catch (TaskException e) {
                                logger.error("Error executing tasks on server " + server.getServerId() + ": " + e.getMessage() + ". Retrying.");
                                retryTask(task, 1);
                            }
                        }
                    }
                    executionMap.put(server.getServerId(), completedTasks);
                } catch (Exception e) {
                    throw new SchedulerException("Error executing tasks on server: " + e.getMessage());
                }
            }

            return executionMap;
        }
    }

    private List<Task> dependencySort(List<Task> prioritySortedTaskList) {
        List<Task> result = new ArrayList<>();

        for (int i = 0; i < prioritySortedTaskList.size(); i++) {
            Task task = prioritySortedTaskList.get(i);
            if (task instanceof DependentTask dependentTask) {
                Set<String> dependencies = dependentTask.getDependencies();

                for (String dep : dependencies) {
                    Task t = prioritySortedTaskList.stream()
                        .filter(c -> Objects.equals(dep, c.getId()))
                        .findFirst()
                        .get();
                    int addingIndex = findIndexById(result, task.getId());
                    if (result.isEmpty() || addingIndex == -1)
                        result.add(t);
                    else
                        result.add(addingIndex, t);

                }
            }
            result.add(task);
        }

        return result.stream().distinct().toList(); // Remove duplicates and return as a list
    }

    private int findIndexById(List<Task> taskList, String id) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public Server findLeastLoadedServer() {
        return serverList.keySet().stream().filter(c -> !circuitClosedServers.contains(c.getServerId())).min(Comparator.comparingInt(Server::getTaskCount)).orElse(null);
    }

    public List<Server> getServers() {
        synchronized (lock) {
            return Collections.unmodifiableList(new ArrayList<>(serverList.keySet()));
        }
    }
}
