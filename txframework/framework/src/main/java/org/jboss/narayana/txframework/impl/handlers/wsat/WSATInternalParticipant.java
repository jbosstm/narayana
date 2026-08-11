package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wst.SystemException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.PostCommit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.Rollback;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Error;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBAParticipantRegistry;

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
