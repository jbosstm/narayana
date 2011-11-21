package org.jboss.jbossts.txframework.impl.handlers.wsba;

import com.arjuna.wst.*;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsba.Complete;

public class WSBACoordinatorCompletionParticipant  extends WSBAParticipantCompletionParticipant implements
        BusinessAgreementWithCoordinatorCompletionParticipant
{
    public WSBACoordinatorCompletionParticipant(Object serviceImpl)
    {
        super(serviceImpl);

        registerEventsOfInterest(Complete.class);
    }

    public void complete() throws WrongStateException, SystemException
    {
        invoke(Complete.class);
    }
}
