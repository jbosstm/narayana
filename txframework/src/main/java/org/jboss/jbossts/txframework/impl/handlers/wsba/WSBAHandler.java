package org.jboss.jbossts.txframework.impl.handlers.wsba;

import com.arjuna.wst11.BAParticipantManager;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsba.Completes;
import org.jboss.jbossts.txframework.api.annotation.management.TxManagement;
import org.jboss.jbossts.txframework.api.exception.TXFrameworkException;
import org.jboss.jbossts.txframework.api.management.WSBATxControl;
import org.jboss.jbossts.txframework.impl.handlers.ProtocolHandler;
import org.jboss.jbossts.txframework.impl.handlers.ParticipantRegistrationException;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class WSBAHandler implements ProtocolHandler
{
    private Method serviceMethod;
    private Object serviceImpl;
    private WSBATxControl wsbaTxControl;
    private BAParticipantManager participantManager;

    public WSBAHandler(Object serviceImpl, Method serviceMethod) throws TXFrameworkException
    {
        this.serviceImpl = serviceImpl;
        this.serviceMethod = serviceMethod;
        participantManager = registerParticipants(serviceImpl, serviceMethod);
        wsbaTxControl = new WSBATxControlImpl(participantManager);

        injectTxManagement(wsbaTxControl);
    }

    protected abstract BAParticipantManager registerParticipants(Object serviceImpl, Method serviceMethod) throws ParticipantRegistrationException;

    //todo: Use CDI to do injection
    private void injectTxManagement(WSBATxControl wsbaTxControl) throws TXFrameworkException
    {
        Field[] fields = serviceImpl.getClass().getFields();
        for (Field field : serviceImpl.getClass().getFields())
        {
            if (field.getAnnotation(TxManagement.class) != null)
            {
                try
                {
                    //todo: check field type
                    field.set(serviceImpl, wsbaTxControl);
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
            Object result = ic.proceed();
            //todo: find a better way of getting the current status of the TX
            if (shouldComplete(serviceMethod) && !((WSBATxControlImpl) wsbaTxControl).isCannotComplete())
            {
                participantManager.completed();
            }
            return result;
        }
        //todo: Should this not be throwable?
        catch (Exception e)
        {
            //Something went wrong, so notify cannot completed
            participantManager.cannotComplete();
            throw e;
        }
    }

    private boolean shouldComplete(Method serviceMethod)
    {
        Completes completes = serviceMethod.getAnnotation(Completes.class);
        return completes != null;
    }

}
