package edu.cwru.ooa21.taskscheduler.tasks;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;
import edu.cwru.ooa21.taskscheduler.interfaces.Task;
import edu.cwru.ooa21.taskscheduler.models.Duration;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.DependentTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DependentTaskTest {

    private DependentTask dependentTask;
    private Duration estimatedDuration;
    private Duration timeOut;

    @BeforeEach
    void setUp() {
        estimatedDuration = Duration.ofMillis(1000); // 1 second
        timeOut = Duration.ofMillis(2000); // 2 seconds
        Set<String> dependencies = new HashSet<>();
        dependencies.add("dependency1");
        dependencies.add("dependency2");
        dependentTask = new DependentTask("task1", estimatedDuration, TaskPriority.HIGH, timeOut, dependencies);
    }

    @Test
    void testGetDependencies() {
        // Check that the dependencies are returned correctly
        Set<String> dependencies = dependentTask.getDependencies();
        assertEquals(2, dependencies.size(), "The task should have 2 dependencies.");
        assertTrue(dependencies.contains("dependency1"), "Dependencies should include 'dependency1'.");
        assertTrue(dependencies.contains("dependency2"), "Dependencies should include 'dependency2'.");
    }

    @Test
    void testCopy() {
        // Create a copy of the dependent task
        Task copiedTask = dependentTask.copy();

        // Ensure the copied task has the same properties but is a distinct object
        assertEquals(dependentTask.getId(), copiedTask.getId(), "Copied task ID should match.");
        assertEquals(dependentTask.getEstimatedDuration(), copiedTask.getEstimatedDuration(), "Copied task duration should match.");
        assertEquals(dependentTask.getTimeOut(), copiedTask.getTimeOut(), "Copied task timeout should match.");
        assertEquals(dependentTask.getPriority(), ((DependentTask) copiedTask).getPriority(), "Copied task priority should match.");
        assertNotSame(dependentTask, copiedTask, "The copied task should not be the same instance.");

        // Check that dependencies are also copied
        Set<String> copiedDependencies = ((DependentTask) copiedTask).getDependencies();
        assertEquals(2, copiedDependencies.size(), "Copied task should have 2 dependencies.");
        assertTrue(copiedDependencies.contains("dependency1"), "Copied dependencies should include 'dependency1'.");
        assertTrue(copiedDependencies.contains("dependency2"), "Copied dependencies should include 'dependency2'.");
    }
}
