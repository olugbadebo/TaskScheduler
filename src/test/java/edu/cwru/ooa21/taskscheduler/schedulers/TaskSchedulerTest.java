package edu.cwru.ooa21.taskscheduler.schedulers;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.exceptions.SchedulerException;
import edu.cwru.ooa21.taskscheduler.exceptions.SchedulerFullException;
import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.DependentTask;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.PriorityTask;
import edu.cwru.ooa21.taskscheduler.models.Duration;
import edu.cwru.ooa21.taskscheduler.models.RemoteServer;
import edu.cwru.ooa21.taskscheduler.models.Server;
import edu.cwru.ooa21.taskscheduler.policies.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class TaskSchedulerTest {
    private TaskScheduler taskScheduler;
    private RetryPolicy mockRetryPolicy;
    private PriorityTask mockPriorityTask1;
    private PriorityTask mockPriorityTask2;
    private DependentTask mockDependentTask;
    private Server mockServer;
    private Server mockServer2;
    private RemoteServer mockRemoteServer;
    private RemoteServer mockRemoteServer2;

    @BeforeEach
    void setUp() {
        mockRetryPolicy = mock(RetryPolicy.class);
        taskScheduler = new TaskScheduler(5, mockRetryPolicy);  // Capacity is set to 5

        mockPriorityTask1 = mock(PriorityTask.class);
        mockPriorityTask2 = mock(PriorityTask.class);
        mockDependentTask = mock(DependentTask.class);
        mockServer = mock(Server.class);
        mockServer2 = mock(Server.class);
        mockRemoteServer = mock(RemoteServer.class);
        mockRemoteServer2 = mock(RemoteServer.class);

        when(mockRemoteServer2.getAttempts()).thenReturn(4);
        when(mockRemoteServer2.getFailureThreshold()).thenReturn(3);

        // Mock priority levels
        when(mockPriorityTask1.getPriority()).thenReturn(TaskPriority.HIGH);
        when(mockPriorityTask2.getPriority()).thenReturn(TaskPriority.LOW);
        when(mockDependentTask.getPriority()).thenReturn(TaskPriority.MEDIUM);

        // Mock retry policy
        when(mockRetryPolicy.getMaxRetries()).thenReturn(3);
        when(mockRetryPolicy.getDelayInMillis(anyInt())).thenReturn(100L);
    }

    @Test
    void testAddServer() {
        taskScheduler.addServer(mockServer);
        List<Server> servers = taskScheduler.getServers();
        assertEquals(1, servers.size());
    }

    @Test
    void testAddRemoteServer() {
        taskScheduler.addServer(mockRemoteServer);
        List<Server> servers = taskScheduler.getServers();
        assertEquals(1, servers.size());
    }

    @Test
    void testAddServer_Copying() {
        taskScheduler.addServer(mockServer);
        List<Server> servers = taskScheduler.getServers();

        // Verify that the server is deep copied
        assertEquals(1, servers.size());
        assertNotSame(mockServer, servers.get(0)); // Ensure it's not the same instance
    }

    @Test
    void testScheduleTask_AddPriorityTask() throws SchedulerFullException, SchedulerException {
        // Use a spy on the real TaskScheduler instance
        TaskScheduler taskSchedulerSpy = spy(new TaskScheduler(5, mockRetryPolicy));

        // Add the mock server to the scheduler
        taskSchedulerSpy.addServer(mockServer);

        // Mock the server to have 0 tasks initially
        when(mockServer.getTaskCount()).thenReturn(0);
        when(mockServer.getAttempts()).thenReturn(0);
        when(mockServer.getFailureThreshold()).thenReturn(10);

        // Mock the estimated duration of the task to return a valid Duration
        when(mockPriorityTask1.getEstimatedDuration()).thenReturn(Duration.ofMillis(100));  // Example of 1000ms duration
        when(mockPriorityTask1.getId()).thenReturn("Mock server 1");

        // Mock the priority of the task to ensure it is treated as a PriorityTask
        when(mockPriorityTask1.getPriority()).thenReturn(TaskPriority.HIGH);

        // Mock the findLeastLoadedServer to return mockServer
        doReturn(mockServer).when(taskSchedulerSpy).findLeastLoadedServer();

        // Schedule the task
        taskSchedulerSpy.scheduleTask(List.of(mockPriorityTask1));
        DependentTask dependentTask = new DependentTask(mockPriorityTask1.getId(),
            mockPriorityTask1.getEstimatedDuration(),
            TaskPriority.HIGH,
            mockPriorityTask1.getTimeOut(),
            Set.of());

        // Verify that the dependent task is added
        verify(mockServer, times(1)).addTask(dependentTask);
    }

    @Test
    void testScheduleTask_NoServersAvailable() {
        assertThrows(SchedulerFullException.class, () -> taskScheduler.scheduleTask(List.of(mockPriorityTask1)));
    }

    @Test
    void testScheduleTask_SchedulerFull() throws SchedulerFullException {
        taskScheduler = new TaskScheduler(1, mockRetryPolicy); // Capacity of 1
        taskScheduler.addServer(mockServer);

        // Mock the estimated duration of both tasks to return a valid Duration
        when(mockPriorityTask1.getEstimatedDuration()).thenReturn(Duration.ofMillis(100));  // 1000 ms for task 1
        when(mockPriorityTask2.getEstimatedDuration()).thenReturn(Duration.ofMillis(200));  // 2000 ms for task 2

        // Mock priority of tasks
        when(mockPriorityTask1.getPriority()).thenReturn(TaskPriority.HIGH);
        when(mockPriorityTask2.getPriority()).thenReturn(TaskPriority.LOW);

        // Mock the server to have 0 tasks initially
        when(mockServer.getTaskCount()).thenReturn(0);

        // Now scheduling the tasks - this should throw SchedulerFullException since capacity is 1
        assertThrows(SchedulerFullException.class, () -> taskScheduler.scheduleTask(List.of(mockPriorityTask1, mockPriorityTask2)));
    }

    @Test
    void testExecuteAll_SuccessfulExecutionAndCorrectPriorityOrder() throws SchedulerException, TaskException {
        when(mockRetryPolicy.getMaxRetries()).thenReturn(1);
        TaskScheduler taskScheduler = spy(new TaskScheduler(5, mockRetryPolicy));

        when(mockPriorityTask1.getTimeOut()).thenReturn(Duration.ofMillis(500));
        doAnswer(invocation -> {
            Thread.sleep(1000);
            return null;
        }).when(mockPriorityTask1).execute();

        Server server = new Server("TestServer", 12);
        PriorityTask pTask1 = new PriorityTask("PTask1", Duration.ofMillis(100), TaskPriority.MEDIUM, Duration.ofMillis(500));
        PriorityTask pTask2 = new PriorityTask("PTask2", Duration.ofMillis(100), TaskPriority.LOW, Duration.ofMillis(500));
        PriorityTask pTask3 = new PriorityTask("PTask3", Duration.ofMillis(100), TaskPriority.HIGH, Duration.ofMillis(500));
        server.addTask(pTask1);
        server.addTask(pTask2);
        server.addTask(pTask3);
        taskScheduler.addServer(server);

        LinkedHashMap<String, List<Task>> executionMap = taskScheduler.executeAll();

        List<Task> executedTasks = executionMap.get(server.getServerId());

        assertEquals(3, executedTasks.size());

        assertTrue(executedTasks.get(0).isCompleted());
        assertTrue(executedTasks.get(0) instanceof PriorityTask);
        assertEquals(TaskPriority.HIGH, ((PriorityTask) executedTasks.get(0)).getPriority());
        assertEquals(pTask3.getId(), executedTasks.get(0).getId());

        assertTrue(executedTasks.get(1).isCompleted());
        assertTrue(executedTasks.get(1) instanceof PriorityTask);
        assertEquals(TaskPriority.MEDIUM, ((PriorityTask) executedTasks.get(1)).getPriority());
        assertEquals(pTask1.getId(), executedTasks.get(1).getId());

        assertTrue(executedTasks.get(2).isCompleted());
        assertTrue(executedTasks.get(2) instanceof PriorityTask);
        assertEquals(TaskPriority.LOW, ((PriorityTask) executedTasks.get(2)).getPriority());
        assertEquals(pTask2.getId(), executedTasks.get(2).getId());

        assertEquals(1, executionMap.size());
    }


    @Test
    void testExecuteAll_SuccessfulExecutionAndCorrectPriorityOrderAndDependencyOrder() throws SchedulerException, TaskException {
        when(mockRetryPolicy.getMaxRetries()).thenReturn(1);
        TaskScheduler taskScheduler = spy(new TaskScheduler(5, mockRetryPolicy));

        when(mockPriorityTask1.getTimeOut()).thenReturn(Duration.ofMillis(500));
        doAnswer(invocation -> {
            Thread.sleep(10000);
            return null;
        }).when(mockPriorityTask1).execute();

        Server server = new Server("TestServer", 12);
        DependentTask pTask1 = new DependentTask("PTask1", Duration.ofMillis(100), TaskPriority.MEDIUM, Duration.ofMillis(500), Set.of("PTask3"));
        DependentTask pTask2 = new DependentTask("PTask2", Duration.ofMillis(100), TaskPriority.LOW, Duration.ofMillis(500), Set.of());
        DependentTask pTask3 = new DependentTask("PTask3", Duration.ofMillis(100), TaskPriority.LOW, Duration.ofMillis(500), Set.of());
        DependentTask pTask4 = new DependentTask("PTask4", Duration.ofMillis(100), TaskPriority.HIGH, Duration.ofMillis(500), Set.of("PTask1"));
        server.addTask(pTask1);
        server.addTask(pTask2);
        server.addTask(pTask3);
        server.addTask(pTask4);
        taskScheduler.addServer(server);

        LinkedHashMap<String, List<Task>> executionMap = taskScheduler.executeAll();

        List<Task> executedTasks = executionMap.get(server.getServerId());

        assertEquals(4, executedTasks.size());

        assertTrue(executedTasks.get(0).isCompleted());
        assertTrue(executedTasks.get(0) instanceof PriorityTask);
        assertEquals(TaskPriority.LOW, ((PriorityTask) executedTasks.get(0)).getPriority());
        assertEquals(executedTasks.get(0).getId(), "PTask3");

        assertTrue(executedTasks.get(1).isCompleted());
        assertTrue(executedTasks.get(1) instanceof PriorityTask);
        assertEquals(TaskPriority.MEDIUM, ((PriorityTask) executedTasks.get(1)).getPriority());
        assertEquals(executedTasks.get(1).getId(), "PTask1");

        assertTrue(executedTasks.get(2).isCompleted());
        assertTrue(executedTasks.get(2) instanceof PriorityTask);
        assertEquals(TaskPriority.HIGH, ((PriorityTask) executedTasks.get(2)).getPriority());
        assertEquals(executedTasks.get(2).getId(), "PTask4");

        assertTrue(executedTasks.get(3).isCompleted());
        assertTrue(executedTasks.get(3) instanceof PriorityTask);
        assertEquals(TaskPriority.LOW, ((PriorityTask) executedTasks.get(3)).getPriority());
        assertEquals(executedTasks.get(3).getId(), "PTask2");

        assertEquals(1, executionMap.size());
    }


    @Test
    void testExecuteAll_NoServers() {
        assertThrows(SchedulerException.class, () -> taskScheduler.executeAll());
    }


    @Test
    void testRetryTask_MaxRetriesReached() throws TaskException {
        when(mockRetryPolicy.getMaxRetries()).thenReturn(1);

        TaskScheduler taskSchedulerSpy = spy(new TaskScheduler(5, mockRetryPolicy));

        when(mockPriorityTask1.getTimeOut()).thenReturn(Duration.ofMillis(500));
        doAnswer(invocation -> {
            Thread.sleep(10000);
            return null;
        }).when(mockPriorityTask1).execute();

        Server server = new Server("MockServer1", 12);
        // Try to schedule the task, expect it to fail after max retries
        PriorityTask pTask1 = new PriorityTask("PTask1", Duration.ofMillis(1000), TaskPriority.HIGH, Duration.ofMillis(500));
        server.addTask(pTask1);
        taskSchedulerSpy.addServer(server);

        SchedulerException schedulerException = assertThrows(SchedulerException.class, taskSchedulerSpy::executeAll);
        assertEquals("Error executing tasks on server: Exceeded max retries according to policy, attempts: 2.", schedulerException.getMessage()); // Should fail and stop retrying
    }

    @Test
    void testFindLeastLoadedServer() {
        taskScheduler.addServer(mockServer);
        taskScheduler.addServer(mockServer2);

        // Mock task counts on servers
        when(mockServer.getTaskCount()).thenReturn(5);
        when(mockServer2.getTaskCount()).thenReturn(3);

        Server leastLoadedServer = taskScheduler.findLeastLoadedServer();
        assertEquals(mockServer2.getServerId(), leastLoadedServer.getServerId()); // Server 2 has fewer tasks, so it should be selected
    }

    @Test
    void testGetServers() {
        taskScheduler.addServer(mockServer);
        List<Server> servers = taskScheduler.getServers();

        assertEquals(1, servers.size());
        assertNotSame(mockServer, servers.get(0)); // Ensure it's a deep copy
    }

    @Test
    void testCircuitBreakerPatternOnServer() throws SchedulerFullException {
        taskScheduler.addServer(mockRemoteServer2);

        // After failing three times, it'll no longer be considered for loading
        PriorityTask pTask1 = new PriorityTask("PTask1", Duration.ofMillis(100), TaskPriority.MEDIUM, Duration.ofMillis(500));
        PriorityTask pTask2 = new PriorityTask("PTask2", Duration.ofMillis(100), TaskPriority.LOW, Duration.ofMillis(500));
        PriorityTask pTask3 = new PriorityTask("PTask3", Duration.ofMillis(100), TaskPriority.LOW, Duration.ofMillis(500));
        PriorityTask pTask4 = new PriorityTask("PTask4", Duration.ofMillis(100), TaskPriority.HIGH, Duration.ofMillis(500));

        assertThrows(SchedulerException.class, () -> {
            taskScheduler.scheduleTask(List.of(pTask1, pTask2, pTask3, pTask4));
        });
    }
}
