package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Complete;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.util.Map;

public class WSBACoordinatorCompletionParticipant  extends WSBAParticipantCompletionParticipant implements
        BusinessAgreementWithCoordinatorCompletionParticipant
{
    public WSBACoordinatorCompletionParticipant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap, String txid) throws ParticipantRegistrationException
    {
        super(serviceInvocationMeta, txDataMap, txid);

        registerEventsOfInterest(Complete.class);
    }

    public void complete() throws WrongStateException, SystemException
    {
        invoke(Complete.class);
    }
}
