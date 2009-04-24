package com.jboss.transaction.txinterop.webservices.atinterop.participant;

import com.arjuna.ats.arjuna.common.Uid;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.wst.ReadOnly;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropUtil;

/**
 * The VolatileAndDurable volatile 2PC participant
 */
public class VolatileAndDurableVolatile2PCParticipant extends ParticipantAdapter implements Volatile2PCParticipant
{
    /**
     * The current coordination context.
     */
    private final CoordinationContextType coordinationContext ;
    
    /**
     * Construct the participant.
     * @param coordinationContext The coordination context.
     */
    public VolatileAndDurableVolatile2PCParticipant(final CoordinationContextType coordinationContext)
    {
        this.coordinationContext = coordinationContext ;
    }
    
    /**
     * Vote to prepare.
     */
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        try
        {
            ATInteropUtil.registerDurable2PC(coordinationContext, new VolatileAndDurableDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SystemException(th.getMessage()) ;
        }
        return new ReadOnly() ;
    }    
}