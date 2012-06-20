package org.jboss.narayana.txframework.impl.handlers;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import org.jboss.narayana.txframework.api.annotation.transaction.Compensatable;
import org.jboss.narayana.txframework.api.annotation.transaction.Transactional;
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

        Compensatable Compensatable = (Compensatable) serviceClass.getAnnotation(Compensatable.class);
        if (Compensatable != null)
        {
            CompletionType completionType = Compensatable.completionType();
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

        Transactional Transactional = (Transactional) serviceClass.getAnnotation(Transactional.class);
        if (Transactional != null)
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
