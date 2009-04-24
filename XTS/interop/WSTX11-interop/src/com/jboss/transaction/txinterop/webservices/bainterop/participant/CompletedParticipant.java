package com.jboss.transaction.txinterop.webservices.bainterop.participant;

import java.util.TimerTask;

import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine;

public class CompletedParticipant extends ParticipantCompletionParticipantAdapter
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
                engine.completed() ;
            }
        } ;
        TransportTimer.getTimer().schedule(timerTask, 2000) ;
    }
}
