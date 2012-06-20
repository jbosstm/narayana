package org.jboss.narayana.txframework.api.exception;

public class TXFrameworkRuntimeException extends RuntimeException
{
    public TXFrameworkRuntimeException(String message)
    {
        super(message);
    }

    public TXFrameworkRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
