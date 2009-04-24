package com.jboss.transaction.txinterop.webservices.bainterop.participant;

import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

public class DropFirstCompensateParticipant extends CoordinatorCompletionParticipantAdapter
{
    private boolean dropped ;
    
    public synchronized void compensate()
    	throws FaultedException, WrongStateException, SystemException
    {
	if (!dropped)
	{
	    dropped = true ;
	    throw new SystemException("Dropping compensate") ;
	}
    }
}
