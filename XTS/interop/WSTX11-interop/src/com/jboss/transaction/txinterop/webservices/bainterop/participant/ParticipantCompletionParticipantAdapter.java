package com.jboss.transaction.txinterop.webservices.bainterop.participant;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

class ParticipantCompletionParticipantAdapter implements BusinessAgreementWithParticipantCompletionParticipant
{
    public void cancel()
    	throws WrongStateException, SystemException
    {
    }

    public void close()
    	throws WrongStateException, SystemException
    {
    }

    public void compensate()
    	throws FaultedException, WrongStateException, SystemException
    {
    }

    public void error()
    	throws SystemException
    {
    }

    public String status()
    	throws SystemException
    {
	return null ;
    }

    public void unknown()
    	throws SystemException
    {
    }
}
