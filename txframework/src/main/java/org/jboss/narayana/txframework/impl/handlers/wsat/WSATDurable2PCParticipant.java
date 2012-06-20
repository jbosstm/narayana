package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.*;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

public class WSATDurable2PCParticipant extends org.jboss.narayana.txframework.impl.Participant implements Durable2PCParticipant
{
    public WSATDurable2PCParticipant(Object serviceImpl, boolean injectDataManagement) throws ParticipantRegistrationException
    {
        super(serviceImpl, injectDataManagement);

        registerEventsOfInterest(Rollback.class, Commit.class, Prepare.class, Error.class, Unknown.class);
    }

    public void commit() throws WrongStateException, SystemException
    {
        invoke(Commit.class);
    }

    public void rollback() throws WrongStateException, SystemException
    {
        invoke(Rollback.class);
    }

    public void error() throws SystemException
    {
        invoke(Error.class);
    }

    public Vote prepare() throws WrongStateException, SystemException
    {
        //todo check type
        Boolean prepare = (Boolean) invoke(Prepare.class);
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
