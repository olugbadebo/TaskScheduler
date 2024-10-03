package edu.cwru.ooa21.taskscheduler.exceptions;

public class SchedulerException extends Exception
{
    public SchedulerException()
    {
        super();
    }

    public SchedulerException(String message)
    {
        super(message);
    }

    public SchedulerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
