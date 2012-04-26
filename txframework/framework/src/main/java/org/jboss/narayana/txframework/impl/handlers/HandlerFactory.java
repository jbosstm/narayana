package org.jboss.narayana.txframework.impl.handlers;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import org.jboss.narayana.txframework.api.annotation.transaction.AT;
import org.jboss.narayana.txframework.api.annotation.transaction.BA;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.handlers.restat.service.RESTATHandler;
import org.jboss.narayana.txframework.impl.handlers.wsat.WSATHandler;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBACoordinatorCompletionHandler;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBAParticipantCompletionHandler;

import java.lang.reflect.Method;

public class HandlerFactory
{
    //todo: improve the way transaction type is detected.
    public static ProtocolHandler createInstance(Object serviceImpl, Method serviceMethod) throws TXFrameworkException
    {
        Class serviceClass = serviceImpl.getClass();

        BA BA = (BA) serviceClass.getAnnotation(BA.class);
        if (BA != null)
        {
            CompletionType completionType = BA.completionType();
            if (completionType == CompletionType.PARTICIPANT)
            {
                return new WSBAParticipantCompletionHandler(serviceImpl, serviceMethod);
            }
            else if (completionType == CompletionType.COORDINATOR)
            {
                return new WSBACoordinatorCompletionHandler(serviceImpl, serviceMethod);
            }
            else
            {
                throw new UnsupportedProtocolException("Unexpected or null completionType");
            }
        }

        AT AT = (AT) serviceClass.getAnnotation(AT.class);
        if (AT != null)
        {
            if (isWSATTransactionRunning())
            {
                return new WSATHandler(serviceImpl, serviceMethod);
            }
            else //assume it must be a REST-AT transaction running.
            {
                return new RESTATHandler(serviceImpl, serviceMethod);
            }
        }
        throw new UnsupportedProtocolException("Expected to find a transaction type annotation on '" + serviceClass.getName() + "'");
    }

    private static boolean isWSATTransactionRunning()
    {
        UserTransaction ut = UserTransactionFactory.userTransaction();
        return !ut.transactionIdentifier().equals("Unknown");
    }

}
