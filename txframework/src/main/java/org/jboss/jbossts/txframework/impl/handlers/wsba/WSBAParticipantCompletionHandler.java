package org.jboss.jbossts.txframework.impl.handlers.wsba;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import org.jboss.jbossts.txframework.api.exception.TXFrameworkException;
import org.jboss.jbossts.txframework.impl.handlers.ParticipantRegistrationException;
import java.lang.reflect.Method;

public class WSBAParticipantCompletionHandler extends WSBAHandler
{
    private WSBAParticipantCompletionParticipant participant;

    public WSBAParticipantCompletionHandler(Object serviceImpl, Method serviceMethod) throws TXFrameworkException
    {
        super(serviceImpl, serviceMethod);
    }

    @Override
    protected BAParticipantManager registerParticipants(Object serviceImpl, Method serviceMethod) throws ParticipantRegistrationException
    {
        try
        {
            Class serviceClass = serviceImpl.getClass();

            participant = new WSBAParticipantCompletionParticipant(serviceImpl);

            BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager();
            return businessActivityManager.enlistForBusinessAgreementWithParticipantCompletion(participant, serviceClass.getName() + new Uid().toString());
        }
        catch (WrongStateException e)
        {
            throw new ParticipantRegistrationException("Transaction was not in a state in which participants can be registered", e);
        }
        catch (UnknownTransactionException e)
        {
            throw new ParticipantRegistrationException("Can't register a participant as the transaction in unknown", e);
        }
        catch (SystemException e)
        {
            throw new ParticipantRegistrationException("A SystemException occurred when attempting to register a participant", e);

        }
    }
}
