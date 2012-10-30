package org.jboss.narayana.txframework.api.exception;

public class TXControlException extends TXFrameworkException
{
    public TXControlException(String message)
    {
        super(message);
    }

    public TXControlException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
