package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.*;

public class WSATDurable2PCParticipant extends org.jboss.narayana.txframework.impl.Participant implements Durable2PCParticipant
{
    public WSATDurable2PCParticipant(Object serviceImpl)
    {
        super(serviceImpl);

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
        invoke(org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.Error.class);
    }

    public Vote prepare() throws WrongStateException, SystemException
    {
        //todo check type
        Vote vote = (Vote) invoke(Prepare.class);
        if (vote == null)
        {
            return new Prepared();
        }
        else
        {
            return vote;
        }
    }

    public void unknown() throws SystemException
    {
        invoke(Unknown.class);
    }
}
