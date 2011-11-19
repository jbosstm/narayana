package org.jboss.jbossts.txframework.impl;

import org.jboss.jbossts.txframework.api.exception.TXFrameworkException;

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
