package edu.cwru.ooa21.taskscheduler.exceptions;

public class ServerException extends Exception
{
    public ServerException()
    {
        super();
    }

    public ServerException(String message)
    {
        super(message);
    }

    public ServerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
