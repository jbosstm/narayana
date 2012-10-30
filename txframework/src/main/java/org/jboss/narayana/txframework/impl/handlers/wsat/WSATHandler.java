package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.Participant;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;

import javax.interceptor.InvocationContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WSATHandler implements ProtocolHandler
{
    private static final WSATParticipantRegistry participantRegistry = new WSATParticipantRegistry();

    private static final Map<String, Participant> durableServiceParticipants = new HashMap<String, Participant>();

    public WSATHandler(ServiceInvocationMeta serviceInvocationMeta) throws TXFrameworkException
    {
        try
        {
            synchronized (participantRegistry)
            {
                String txid = UserTransactionFactory.userTransaction().toString();

                //Only create participant if there is not already a participant for this ServiceImpl and this transaction
                Participant participantToResume;
                if (!participantRegistry.isRegistered(txid, serviceInvocationMeta.getServiceClass()))
                {
                    Map txDataMap = new HashMap();
                    Volatile2PCParticipant volatileParticipant = new WSATVolatile2PCParticipant(serviceInvocationMeta, txDataMap, txid);
                    Durable2PCParticipant durableParticipant = new WSATDurable2PCParticipant(serviceInvocationMeta, txDataMap);

                    TransactionManager transactionManager = TransactionManagerFactory.transactionManager();
                    String idPrefix = serviceInvocationMeta.getServiceClass().getName();
                    transactionManager.enlistForVolatileTwoPhase(volatileParticipant, idPrefix + UUID.randomUUID());
                    transactionManager.enlistForDurableTwoPhase(durableParticipant, idPrefix + UUID.randomUUID());

                    participantRegistry.register(txid, serviceInvocationMeta.getServiceClass());

                    synchronized (durableServiceParticipants)
                    {
                        participantToResume = (Participant) durableParticipant;
                        durableServiceParticipants.put(txid, participantToResume);
                    }
                }
                else
                {
                    participantToResume = durableServiceParticipants.get(txid);
                }
                participantToResume.resume();
            }
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

    @Override
    public Object proceed(InvocationContext ic) throws Exception
    {
        return ic.proceed();
    }

    @Override
    public void notifySuccess() {
        Participant.suspend();
    }

    @Override
    public void notifyFailure() {
        //Todo: ensure transaction rolled back
        Participant.suspend();
    }

}
