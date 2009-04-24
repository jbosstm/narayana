package com.jboss.transaction.txinterop.webservices.atinterop.participant;

import com.arjuna.wst.Aborted;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

/**
 * The rollback durable 2PC participant
 */
public class RollbackDurable2PCParticipant extends ParticipantAdapter implements Durable2PCParticipant
{
    /**
     * Vote to abort.
     */
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        return new Aborted() ;
    }    
}