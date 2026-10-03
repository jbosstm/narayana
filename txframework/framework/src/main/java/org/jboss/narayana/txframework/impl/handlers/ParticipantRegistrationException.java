package org.jboss.narayana.txframework.impl.handlers;

import org.jboss.narayana.txframework.api.exception.TXFrameworkException;

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
