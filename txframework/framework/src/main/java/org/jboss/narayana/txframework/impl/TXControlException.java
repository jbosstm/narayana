package org.jboss.narayana.txframework.impl;

import org.jboss.narayana.txframework.api.exception.TXFrameworkException;

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
