package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.Aborted;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Unknown;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.util.Map;

public class WSATDurable2PCParticipant extends org.jboss.narayana.txframework.impl.Participant implements Durable2PCParticipant
{
    public WSATDurable2PCParticipant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap) throws ParticipantRegistrationException
    {
        super(serviceInvocationMeta, txDataMap);

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
