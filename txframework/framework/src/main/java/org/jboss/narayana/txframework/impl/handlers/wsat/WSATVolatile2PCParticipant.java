package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.*;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

public class WSATVolatile2PCParticipant extends org.jboss.narayana.txframework.impl.Participant implements Volatile2PCParticipant
{
    public WSATVolatile2PCParticipant(Object serviceImpl, boolean injectDataManagement) throws ParticipantRegistrationException
    {
        super(serviceImpl, injectDataManagement);
        registerEventsOfInterest(PrePrepare.class, PostCommit.class, Rollback.class, Error.class, Unknown.class);
    }

    public void commit() throws WrongStateException, SystemException
    {
        invoke(PostCommit.class);
    }

    public void rollback() throws WrongStateException, SystemException
    {
        invoke(Rollback.class);
    }

    public void error() throws SystemException
    {
        invoke(org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error.class);
    }

    public Vote prepare() throws WrongStateException, SystemException
    {
        //todo check type
        Boolean prepare = (Boolean) invoke(PrePrepare.class);
        if (prepare == null)
        {
            return new Prepared();
        }
        else if (prepare)
        {
            return new Prepared();
        }
        else
        {
            return new Aborted();
        }
    }

    public void unknown() throws SystemException
    {
        invoke(Unknown.class);
    }
}
