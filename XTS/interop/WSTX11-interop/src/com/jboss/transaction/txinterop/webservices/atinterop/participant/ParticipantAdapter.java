package com.jboss.transaction.txinterop.webservices.atinterop.participant;

import com.arjuna.wst.Participant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

/**
 * The base participant adapter.
 */
public abstract class ParticipantAdapter implements Participant
{
    /**
     * Commit the participant.
     */
    public void commit()
        throws WrongStateException, SystemException
    {
    }

    /**
     * Rollback the participant.
     */
    public void rollback()
        throws WrongStateException, SystemException
    {
    }
    
    /**
     * Handle an error on the participant.
     */
    public void error()
        throws SystemException
    {
    }

    /**
     * Handle an unknown on the participant.
     */
    public void unknown()
        throws SystemException
    {
    }

}
