package edu.cwru.ooa21.taskscheduler.tasks;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.models.Duration;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.PriorityTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriorityTaskTest {

    private PriorityTask priorityTask;
    private Duration estimatedDuration;
    private Duration timeOut;

    @BeforeEach
    void setUp() {
        estimatedDuration = Duration.ofMillis(1000); // 1 second
        timeOut = Duration.ofMillis(2000); // 2 seconds
        priorityTask = new PriorityTask("task1", estimatedDuration, TaskPriority.HIGH, timeOut);
    }

    @Test
    void testGetPriority() {
        // Check that the priority is set correctly
        assertEquals(TaskPriority.HIGH, priorityTask.getPriority(), "The priority should be HIGH.");
    }

    @Test
    void testCopy() {
        // Create a copy of the priority task
        Task copiedTask = priorityTask.copy();

        // Ensure the copied task has the same properties but is a distinct object
        assertEquals(priorityTask.getId(), copiedTask.getId(), "Copied task ID should match.");
        assertEquals(priorityTask.getEstimatedDuration(), copiedTask.getEstimatedDuration(), "Copied task duration should match.");
        assertEquals(priorityTask.getTimeOut(), copiedTask.getTimeOut(), "Copied task timeout should match.");
        assertEquals(priorityTask.getPriority(), ((PriorityTask) copiedTask).getPriority(), "Copied task priority should match.");
        assertNotSame(priorityTask, copiedTask, "The copied task should not be the same instance.");
    }
}
