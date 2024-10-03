package edu.cwru.ooa21.taskscheduler.exceptions;

public class TaskException extends Exception
{
    public TaskException()
    {
        super();
    }

    public TaskException(String message)
    {
        super(message);
    }

    public TaskException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
