package edu.cwru.ooa21.taskscheduler.enums;

public enum TaskPriority
{
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int priority;

    TaskPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
