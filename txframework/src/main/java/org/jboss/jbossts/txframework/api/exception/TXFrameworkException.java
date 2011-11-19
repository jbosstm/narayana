package org.jboss.jbossts.txframework.api.exception;

public class TXFrameworkException extends Exception
{
    public TXFrameworkException(String message)
    {
        super(message);
    }

    public TXFrameworkException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
