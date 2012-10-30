package org.jboss.narayana.txframework.impl.handlers.restat.service;

import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.util.Map;

public class RESTAT2PCParticipant extends org.jboss.narayana.txframework.impl.Participant
{
    public RESTAT2PCParticipant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap) throws ParticipantRegistrationException
    {
        super(serviceInvocationMeta, txDataMap);

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
        Boolean vote = (Boolean) invoke(Prepare.class);
        if (vote == null)
        {
            return true;
        }
        else
        {
            return vote;
        }
    }
}
