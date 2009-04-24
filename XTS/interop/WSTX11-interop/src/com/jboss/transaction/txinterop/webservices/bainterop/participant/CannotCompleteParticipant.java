package com.jboss.transaction.txinterop.webservices.bainterop.participant;

import java.util.TimerTask;

import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionParticipantEngine;

public class CannotCompleteParticipant extends CoordinatorCompletionParticipantAdapter
{
    private CoordinatorCompletionParticipantEngine engine ;
    
    public void setEngine(final CoordinatorCompletionParticipantEngine engine)
    {
	this.engine = engine ;
    }
    
    public void initialiseTimeout()
    {
        final TimerTask timerTask = new TimerTask() {
            public void run() {
                engine.cannotComplete() ;
            }
        } ;
        TransportTimer.getTimer().schedule(timerTask, 2000) ;
    }
}
