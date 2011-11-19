package org.jboss.jbossts.txframework.impl.handlers;

import org.jboss.jbossts.txframework.api.exception.TXFrameworkException;

public class ParticipantRegistrationException extends TXFrameworkException
{
    public ParticipantRegistrationException(String message)
    {
        super(message);
    }

    public ParticipantRegistrationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
