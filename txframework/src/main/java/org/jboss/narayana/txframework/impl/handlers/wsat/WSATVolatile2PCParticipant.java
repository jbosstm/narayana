package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.Aborted;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.PostCommit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.PrePrepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Unknown;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.util.Map;

public class WSATVolatile2PCParticipant extends org.jboss.narayana.txframework.impl.Participant implements Volatile2PCParticipant
{
    protected final WSATParticipantRegistry participantRegistry = new WSATParticipantRegistry();
    private String txid;

    public WSATVolatile2PCParticipant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap, String txid) throws ParticipantRegistrationException
    {
        super(serviceInvocationMeta, txDataMap);
        this.txid = txid;
        
        registerEventsOfInterest(PrePrepare.class, PostCommit.class, Rollback.class, Error.class, Unknown.class);
    }

    public void commit() throws WrongStateException, SystemException
    {
        invoke(PostCommit.class);
        participantRegistry.forget(txid);
    }

    public void rollback() throws WrongStateException, SystemException
    {
        invoke(Rollback.class);
        participantRegistry.forget(txid);
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
