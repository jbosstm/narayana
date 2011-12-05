package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.*;

public class WSATVolatile2PCParticipant extends org.jboss.narayana.txframework.impl.Participant implements Volatile2PCParticipant
{
    public WSATVolatile2PCParticipant(Object serviceImpl)
    {
        super(serviceImpl);

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
        invoke(org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.Error.class);
    }

    public Vote prepare() throws WrongStateException, SystemException
    {
        Vote vote = (Vote) invoke(PrePrepare.class);
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
