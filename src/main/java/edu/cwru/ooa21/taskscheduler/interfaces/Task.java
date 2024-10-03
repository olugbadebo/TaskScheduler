package edu.cwru.ooa21.taskscheduler.interfaces;
import edu.cwru.ooa21.taskscheduler.models.Duration;
import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.exceptions.TaskException;

import java.io.Serializable;
import java.util.Set;

public interface Task extends Serializable
{
    String getId();
    void execute() throws TaskException;
    boolean isCompleted();
    Duration getEstimatedDuration();
    Duration getTimeOut();
    Task copy();
    void cleanup();
}
