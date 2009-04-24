package com.jboss.transaction.wstf.webservices.sc007.participant;

import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

/**
 * The commit durable 2PC participant
 */
public class CommitDurable2PCParticipant extends ParticipantAdapter implements Durable2PCParticipant
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