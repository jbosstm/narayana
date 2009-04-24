package com.jboss.transaction.txinterop.webservices.atinterop.participant;

import java.util.TimerTask;

import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.messaging.engines.ParticipantEngine;

/**
 * The durable 2PC participant which fails the first call to commit and recovers.
 */
public class CommitFailureRecoveryDurable2PCParticipant extends ParticipantAdapter implements Durable2PCParticipant
{
    /**
     * The participant engine.
     */
    private ParticipantEngine engine ;
    /**
     * The set recovery flag.
     */
    private boolean setRecovery ;
    /**
     * The recovering flag.
     */
    private boolean recovering ;
    
    /**
     * Set the participant engine.
     * @param engine The participant engine.
     */
    public void setEngine(final ParticipantEngine engine)
    {
        this.engine = engine ;
    }
    
    /**
     * Vote to prepare.
     */
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        return new Prepared() ;
    }
    
    public void commit()
        throws WrongStateException, SystemException
    {
        if (!setRecovery)
        {
            setRecovery = true ;
            final TimerTask timerTask = new TimerTask() {
                public void run() {
                    recovering = true ;
                    engine.recovery() ;
                }
            } ;
            TransportTimer.getTimer().schedule(timerTask, 2000) ;
        }
        
        if (!recovering)
        {
            throw new IllegalStateException("Forced failure of commit") ;
        }
    }
}