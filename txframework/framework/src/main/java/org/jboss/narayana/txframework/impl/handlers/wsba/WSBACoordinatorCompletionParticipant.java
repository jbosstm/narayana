package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Complete;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

public class WSBACoordinatorCompletionParticipant  extends WSBAParticipantCompletionParticipant implements
        BusinessAgreementWithCoordinatorCompletionParticipant
{
    public WSBACoordinatorCompletionParticipant(Object serviceImpl, boolean injectDataManagement) throws ParticipantRegistrationException
    {
        super(serviceImpl, injectDataManagement);

        registerEventsOfInterest(Complete.class);
    }

    public void complete() throws WrongStateException, SystemException
    {
        invoke(Complete.class);
    }
}
