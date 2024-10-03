package edu.cwru.ooa21.taskscheduler.models;

import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.DependentTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServerTest {

    private Server server;
    private DependentTask mockTask1;
    private DependentTask mockTask2;
    private DependentTask mockTask3;
//    private Task mockTask4;

    @BeforeEach
    void setUp() {
        server = new Server("server1", 12);

        // Mocking Task objects
        mockTask1 = mock(DependentTask.class);
        mockTask2 = mock(DependentTask.class);
        mockTask3 = mock(DependentTask.class);

        // Mock task durations
        when(mockTask1.getEstimatedDuration()).thenReturn(Duration.ofMillis(100));
        when(mockTask2.getEstimatedDuration()).thenReturn(Duration.ofMillis(200));
        when(mockTask3.getEstimatedDuration()).thenReturn(Duration.ofMillis(500));
//        when(mockTask4.getEstimatedDuration()).thenReturn(Duration.ofMillis(2000));

        // Mock task completion status
        when(mockTask1.isCompleted()).thenReturn(false);
        when(mockTask2.isCompleted()).thenReturn(false);
        when(mockTask3.isCompleted()).thenReturn(false);
//        when(mockTask4.isCompleted()).thenReturn(false);

        when(mockTask3.getTimeOut()).thenReturn(Duration.ofMillis(500));
//        doThrow(new TaskException("Task failed")).when(mockTask4).execute();
    }

    @Test
    void testAddTask() {
        server.addTask(mockTask1);
        assertEquals(1, server.getTaskCount());

        server.addTask(mockTask2);
        assertEquals(2, server.getTaskCount());
    }

    @Test
    void testAddNullAsTaskThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> server.addTask(null));
    }

    @Test
    void testGetTasks() {
        server.addTask(mockTask1);
        server.addTask(mockTask2);
        List<Task> tasks = server.getTasks();

        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(mockTask1));
        assertTrue(tasks.contains(mockTask2));
    }

    @Test
    void testGetServerId() {
        assertEquals("server1", server.getServerId());
    }

    @Test
    void testGetTaskCount() {
        assertEquals(0, server.getTaskCount()); // Initially, the task queue should be empty

        server.addTask(mockTask1);
        assertEquals(1, server.getTaskCount());

        server.addTask(mockTask2);
        assertEquals(2, server.getTaskCount());
    }

    @Test
    void testExecuteTasks_Success() throws TaskException {
        // Set tasks to complete successfully when executed
        when(mockTask1.isCompleted()).thenReturn(false).thenReturn(true); // Before and after execution
        when(mockTask2.isCompleted()).thenReturn(false).thenReturn(true);
        doNothing().when(mockTask1).execute();
        doNothing().when(mockTask2).execute();

        server.addTask(mockTask1);
        server.addTask(mockTask2);

        List<Task> completedTasks = server.executeTasks();
        assertEquals(2, completedTasks.size());
        assertTrue(completedTasks.contains(mockTask1));
        assertTrue(completedTasks.contains(mockTask2));
        assertEquals(0, server.getTaskCount()); // All tasks should be removed after execution

        verify(mockTask1).execute();
        verify(mockTask2).execute();
    }

    @Test
    void testExecuteTasks_ExecutionFailure() throws TaskException {
        // Simulate task execution failure for mockTask1
        doThrow(TaskException.class).when(mockTask1).execute();

        // Simulate task2 execution success
        doNothing().when(mockTask2).execute();  // mockTask2 executes without throwing an exception

        // Ensure mockTask2 is initially not completed, but is marked as completed after execution
        when(mockTask2.isCompleted()).thenReturn(false).thenReturn(true);  // Before and after execution
        when(mockTask1.isCompleted()).thenReturn(true).thenReturn(false);

        // Add tasks to the server
        server.addTask(mockTask1);
        server.addTask(mockTask2);

        // Execute tasks and capture the list of completed tasks
        List<Task> completedTasks = server.executeTasks();

        // Assert that only task 2 was completed
        assertEquals(1, completedTasks.size());  // Only mockTask2 should be completed
        assertTrue(completedTasks.contains(mockTask2));  // mockTask2 should be in the list of completed tasks
        assertFalse(completedTasks.contains(mockTask1));  // mockTask2 should be in the list of completed tasks
        assertEquals(1, server.getTaskCount());  // Only mockTask1 should remain in the task queue

        verify(mockTask2).execute();
    }

    @Test
    void testExecuteTasks_Timeout() throws TaskException {
        when(mockTask3.getTimeOut()).thenReturn(Duration.ofMillis(500));

        server.addTask(mockTask3);

        doAnswer(invocation -> {
            Thread.sleep(100000);
            return null;
        }).when(mockTask3).execute();

        when(mockTask3.isCompleted()).thenReturn(false);

        TaskException thrown = assertThrows(TaskException.class, () -> server.executeTasks());

        assertEquals("Error executing tasks: Task %s timed out.".formatted(mockTask3.getId()), thrown.getMessage());
        assertEquals(1, server.getTaskCount());
    }

    @Test
    void testGetPendingTasks() {
        server.addTask(mockTask1);
        server.addTask(mockTask2);

        List<Task> pendingTasks = server.getFailedTasks();
        assertEquals(2, pendingTasks.size());

        when(mockTask1.isCompleted()).thenReturn(true); // Mark task1 as completed
        pendingTasks = server.getFailedTasks();
        assertEquals(1, pendingTasks.size()); // Now only task 2 should remain incomplete
    }

    @Test
    void testGetServerDuration() {
        server.addTask(mockTask1);
        server.addTask(mockTask2);

        assertEquals(Duration.ofMillis(3000), server.getServerDuration()); // 1000 + 2000 = 3000 ms
    }

    @Test
    void testCopyConstructor() {
        server.addTask(mockTask1);
        server.addTask(mockTask2);

        Server copiedServer = new Server(server);
        assertEquals(server.getServerId(), copiedServer.getServerId());
        assertEquals(server.getServerDuration(), copiedServer.getServerDuration());
        assertEquals(server.getTaskCount(), copiedServer.getTaskCount());
        assertNotSame(server.getTasks(), copiedServer.getTasks()); // Ensure deep copy
    }
}
