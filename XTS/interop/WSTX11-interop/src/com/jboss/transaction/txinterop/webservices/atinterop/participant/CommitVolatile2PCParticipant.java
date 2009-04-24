package com.jboss.transaction.txinterop.webservices.atinterop.participant;

import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

/**
 * The commit volatile 2PC participant
 */
public class CommitVolatile2PCParticipant extends ParticipantAdapter implements Volatile2PCParticipant
{
    /**
     * Vote to prepare.
     */
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        return new Prepared() ;
    }    
}