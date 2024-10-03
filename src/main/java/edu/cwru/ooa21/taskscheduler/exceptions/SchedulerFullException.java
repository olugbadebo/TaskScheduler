package edu.cwru.ooa21.taskscheduler.exceptions;

public class SchedulerFullException extends Exception
{
    public SchedulerFullException()
    {
        super();
    }

    public SchedulerFullException(String message)
    {
        super(message);
    }

    public SchedulerFullException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
