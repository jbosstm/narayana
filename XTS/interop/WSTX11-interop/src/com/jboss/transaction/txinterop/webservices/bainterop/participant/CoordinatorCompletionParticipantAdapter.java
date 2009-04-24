package com.jboss.transaction.txinterop.webservices.bainterop.participant;

import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

class CoordinatorCompletionParticipantAdapter extends ParticipantCompletionParticipantAdapter implements BusinessAgreementWithCoordinatorCompletionParticipant
{
    public void complete()
        throws WrongStateException, SystemException
    {
    }
}
