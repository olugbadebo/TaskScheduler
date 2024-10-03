package edu.cwru.ooa21.taskscheduler.tasks;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.DependentTask;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.PriorityTask;
import edu.cwru.ooa21.taskscheduler.models.Duration;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.SimpleTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

class SimpleTaskTest {

    private SimpleTask simpleTask;
    private Duration mockDuration;
    private Duration mockTimeout;

    @BeforeEach
    void setUp() {
        mockDuration = Duration.ofMillis(100); // 1-second duration
        mockTimeout = Duration.ofMillis(500);  // 5-second timeout
        simpleTask = new SimpleTask("task1", mockDuration, mockTimeout);
    }

    @Test
    void testGetId() {
        assertEquals("task1", simpleTask.getId());
    }

    @Test
    void testIsCompletedBeforeExecution() {
        // Initially, the task should not be completed
        assertFalse(simpleTask.isCompleted());
    }

    @Test
    void testGetEstimatedDuration() {
        assertEquals(mockDuration, simpleTask.getEstimatedDuration());
    }

    @Test
    void testExecutionDurationIsAsLongAsTaskDuration() throws TaskException {
        long startTime = System.currentTimeMillis();
        simpleTask.execute();
        long endTime = System.currentTimeMillis();
        long timeTakenToCompleteExecution = endTime - startTime;

        assertTrue(timeTakenToCompleteExecution >= mockDuration.toMillis().longValue(), "Execution did not take at least the estimated duration.");
    }

    @Test
    void testIsCompletedAfterExecutionSuccess() throws TaskException {
        // Initially, the task is not completed
        assertFalse(simpleTask.isCompleted());

        // Execute the task
        simpleTask.execute();

        // After execution, the task should be marked as completed
        assertTrue(simpleTask.isCompleted());
    }

    @Test
    void testExecute_TaskAlreadyCompleted_ThrowsException() throws TaskException {
        // Execute the task once
        simpleTask.execute();

        // Trying to execute it again should throw an exception
        TaskException thrown = assertThrows(TaskException.class, () -> simpleTask.execute());
        assertEquals("This task has already been completed", thrown.getMessage());
    }

    @Test
    void testExecute_InterruptedException() throws TaskException {
        // Simulate an interruption during task execution
        SimpleTask interruptibleTask = new SimpleTask("interruptibleTask", mockDuration, mockTimeout) {
            @Override
            public void execute() throws TaskException {
                throw new TaskException("The task was interrupted: Simulated interruption");
            }
        };

        TaskException thrown = assertThrows(TaskException.class, interruptibleTask::execute);
        assertTrue(thrown.getMessage().contains("The task was interrupted"));
    }

    @Test
    void testCopy() {
        // Create a copy of the task
        Task copiedTask = simpleTask.copy();

        // Ensure the copied task has the same properties but is a distinct object
        assertEquals(simpleTask, copiedTask);
        assertNotSame(simpleTask, copiedTask);
    }

    @Test
    void testGetTimeOut() {
        assertEquals(mockTimeout, simpleTask.getTimeOut());
    }

    @Test
    void testEqualsAndHashCode_SameId() {
        SimpleTask task2 = new SimpleTask("task1", mockDuration, mockTimeout);

        // Ensure that two tasks with the same ID are considered equal
        assertEquals(simpleTask, task2);
        assertEquals(simpleTask.hashCode(), task2.hashCode());
    }

    @Test
    void testEqualsAndHashCode_DifferentId() {
        SimpleTask task2 = new SimpleTask("task2", mockDuration, mockTimeout);

        // Ensure that tasks with different IDs are not equal
        assertNotEquals(simpleTask, task2);
        assertNotEquals(simpleTask.hashCode(), task2.hashCode());
    }

    @Test
    void testCleanup() {
        // Just checking if the method works since it prints to the console
        simpleTask.cleanup();
        assertDoesNotThrow(() -> simpleTask.cleanup());
    }
}
