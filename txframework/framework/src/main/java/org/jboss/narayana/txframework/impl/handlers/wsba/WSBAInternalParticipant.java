package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.wst.SystemException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Error;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

public class WSBAInternalParticipant
{
    protected final WSBAParticipantRegistry participantRegistry = new WSBAParticipantRegistry();
    private String txid;

    public WSBAInternalParticipant() throws ParticipantRegistrationException
    {
        try
        {
            BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager();
            txid = businessActivityManager.currentTransaction().toString();
        }
        catch (SystemException e)
        {
            throw new ParticipantRegistrationException("Unable to lookup current transaction id", e);
        }
    }

    @Close
    @Cancel
    @Compensate
    public void forgetPaticipant()
    {
        participantRegistry.forget(txid);
    }

}
