package org.jboss.narayana.txframework.impl.handlers.restat.service;

import com.arjuna.wst.Aborted;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.Vote;
import javassist.bytecode.annotation.BooleanMemberValue;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.api.exception.TXFrameworkRuntimeException;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

public class RESTAT2PCParticipant extends org.jboss.narayana.txframework.impl.Participant
{
    public RESTAT2PCParticipant(Object serviceImpl, boolean injectDataManagement) throws ParticipantRegistrationException
    {
        super(serviceImpl, injectDataManagement);

        registerEventsOfInterest(Rollback.class, Commit.class, Prepare.class);
    }

    public void commit()
    {
        invoke(Commit.class);
    }

    public void rollback()
    {
        invoke(Rollback.class);
    }

    public boolean prepare()
    {
        Vote vote = (Vote) invoke(Prepare.class);
        if (vote == null)
        {
            return true;
        }
        else if (vote instanceof Aborted)
        {
            return false;
        }
        else if (vote instanceof Prepared)
        {
            return true;
        }
        else
        {
            throw new TXFrameworkRuntimeException("Unexpected vote type: " + vote.getClass().getName());
        }
    }
}
