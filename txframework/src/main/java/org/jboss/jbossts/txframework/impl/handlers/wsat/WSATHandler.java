package org.jboss.jbossts.txframework.impl.handlers.wsat;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.wst.*;
import com.arjuna.wst11.BAParticipantManager;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsba.Completes;
import org.jboss.jbossts.txframework.api.annotation.management.TxManagement;
import org.jboss.jbossts.txframework.api.exception.TXFrameworkException;
import org.jboss.jbossts.txframework.api.management.ATTxControl;
import org.jboss.jbossts.txframework.api.management.WSBATxControl;
import org.jboss.jbossts.txframework.impl.Participant;
import org.jboss.jbossts.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.jbossts.txframework.impl.handlers.ProtocolHandler;
import org.jboss.jbossts.txframework.impl.handlers.wsba.WSBATxControlImpl;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WSATHandler implements ProtocolHandler
{
    private Object serviceImpl;
    private ATTxControl atTxControl;

    public WSATHandler(Object serviceImpl, Method serviceMethod) throws TXFrameworkException
    {
        this.serviceImpl = serviceImpl;

        String idPrefix = serviceImpl.getClass().getName();
        registerParticipants(idPrefix);

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

    public Object proceed(InvocationContext ic) throws Exception
    {
        try
        {
            return ic.proceed();
        }
        //todo: Should this not be throwable?
        catch (Exception e)
        {
            //todo: Something went wrong, so ensure TX rollback
            throw e;
        }
    }

    private void registerParticipants(String idPrefix) throws ParticipantRegistrationException
    {
        try
        {
            Volatile2PCParticipant volatileParticipant = new WSATVolatile2PCParticipant(serviceImpl);
            Durable2PCParticipant durableParticipant = new WSATDurable2PCParticipant(serviceImpl);

            TransactionManager transactionManager = TransactionManagerFactory.transactionManager();
            transactionManager.enlistForVolatileTwoPhase(volatileParticipant, idPrefix + new Uid().toString());
            transactionManager.enlistForDurableTwoPhase(durableParticipant, idPrefix + new Uid().toString());
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
