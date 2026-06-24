package org.jboss.narayana.txframework.impl.handlers;

import org.jboss.narayana.txframework.api.exception.TXFrameworkException;

public class UnsupportedProtocolException extends TXFrameworkException
{
    public UnsupportedProtocolException(String message)
    {
        super(message);
    }

    public UnsupportedProtocolException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
