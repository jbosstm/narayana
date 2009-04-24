package com.jboss.transaction.wstf.webservices.sc007.participant;

import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.ReadOnly;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

/**
 * The readonly durable 2PC participant
 */
public class ReadonlyDurable2PCParticipant extends ParticipantAdapter implements Durable2PCParticipant
{
    /**
     * Vote readonly.
     */
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        return new ReadOnly() ;
    }    
}