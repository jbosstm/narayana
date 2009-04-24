package com.jboss.transaction.txinterop.webservices.bainterop.participant;

import java.util.TimerTask;

import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;

public class FailParticipant extends ParticipantCompletionParticipantAdapter
{
    private ParticipantCompletionParticipantEngine engine ;
    
    public void setEngine(final ParticipantCompletionParticipantEngine engine)
    {
	this.engine = engine ;
    }
    
    public void initialiseTimeout()
    {
        final TimerTask timerTask = new TimerTask() {
            public void run() {
                engine.fail(BAInteropConstants.INTEROP_ELEMENT_QNAME_FAIL) ;
            }
        } ;
        TransportTimer.getTimer().schedule(timerTask, 2000) ;
    }
}
