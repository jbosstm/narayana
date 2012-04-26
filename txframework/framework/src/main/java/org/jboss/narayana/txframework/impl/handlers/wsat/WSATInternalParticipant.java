package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.mw.wst11.UserTransactionFactory;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.PostCommit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

public class WSATInternalParticipant
{
    protected final WSATParticipantRegistry participantRegistry = new WSATParticipantRegistry();
    private String txid;

    public WSATInternalParticipant() throws ParticipantRegistrationException
    {
        txid = UserTransactionFactory.userTransaction().toString();
    }

    @PostCommit
    @Rollback
    public void forgetPaticipant()
    {
        participantRegistry.forget(txid);
    }

}
