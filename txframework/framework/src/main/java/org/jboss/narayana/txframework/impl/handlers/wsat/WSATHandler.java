package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.txframework.api.annotation.management.TxManagement;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.api.management.ATTxControl;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class WSATHandler implements ProtocolHandler
{
    private Object serviceImpl;
    private ATTxControl atTxControl;
    private static final WSATParticipantRegistry participantRegistry = new WSATParticipantRegistry();

    public WSATHandler(Object serviceImpl, Method serviceMethod) throws TXFrameworkException
    {
        this.serviceImpl = serviceImpl;

        String idPrefix = serviceImpl.getClass().getName();

        //Register Service's participant
        registerParticipants(serviceImpl, idPrefix);

        //Register internal participant for tidy up at end of TX
        registerParticipants(new WSATInternalParticipant(), WSATInternalParticipant.class.getName());

        atTxControl = new ATTxControlImpl();
        injectTxManagement(atTxControl);
    }

    //todo: Use CDI to do injection
    private void injectTxManagement(ATTxControl atTxControl) throws TXFrameworkException
    {
        Field[] fields = serviceImpl.getClass().getFields();
        for (Field field : serviceImpl.getClass().getFields())
        {
            if (field.getAnnotation(TxManagement.class) != null)
            {
                try
                {
                    //todo: check field type
                    field.set(serviceImpl, atTxControl);
                }
                catch (IllegalAccessException e)
                {
                    throw new TXFrameworkException("Unable to inject TxManagement impl to field '" + field.getName() + "' on '" + serviceImpl.getClass().getName() + "'", e);
                }
            }
        }
        //didn't find an injection point. No problem as this is optional
    }

    @Override
    public Object proceed(InvocationContext ic) throws Exception
    {
        return ic.proceed();
    }

    @Override
    public void notifySuccess() {
        //do nothing
    }

    @Override
    public void notifyFailure() {
        //Todo: ensure transaction rolled back
    }

    private void registerParticipants(Object participant, String idPrefix) throws ParticipantRegistrationException
    {
        try
        {
            synchronized (participantRegistry)
            {
                String txid = UserTransactionFactory.userTransaction().toString();

                //Only create participant if there is not already a participant for this ServiceImpl and this transaction
                if (!participantRegistry.isRegistered(txid, participant.getClass()))
                {
                    //Internal participant for doing tidy up at the end of the transaction
                    Volatile2PCParticipant volatileParticipant = new WSATVolatile2PCParticipant(participant);
                    Durable2PCParticipant durableParticipant = new WSATDurable2PCParticipant(participant);


                    TransactionManager transactionManager = TransactionManagerFactory.transactionManager();
                    transactionManager.enlistForVolatileTwoPhase(volatileParticipant, idPrefix + UUID.randomUUID());
                    transactionManager.enlistForDurableTwoPhase(durableParticipant, idPrefix + UUID.randomUUID());

                    participantRegistry.register(txid, participant.getClass());
                }
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

}
