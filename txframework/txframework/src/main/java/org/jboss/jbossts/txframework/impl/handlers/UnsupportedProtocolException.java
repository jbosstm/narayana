package org.jboss.jbossts.txframework.impl.handlers;

import org.jboss.jbossts.txframework.api.exception.TXFrameworkException;

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
