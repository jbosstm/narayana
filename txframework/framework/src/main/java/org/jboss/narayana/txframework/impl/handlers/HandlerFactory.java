package org.jboss.narayana.txframework.impl.handlers;

import org.jboss.narayana.txframework.api.annotation.transaction.RESTAT;
import org.jboss.narayana.txframework.api.annotation.transaction.WSAT;
import org.jboss.narayana.txframework.api.annotation.transaction.WSBA;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.handlers.restat.service.RESTATHandler;
import org.jboss.narayana.txframework.impl.handlers.wsat.WSATHandler;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBACoordinatorCompletionHandler;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBAParticipantCompletionHandler;
import java.lang.reflect.Method;

public class HandlerFactory
{
    public static ProtocolHandler createInstance(Object serviceImpl, Method serviceMethod) throws TXFrameworkException
    {
        Class serviceClass = serviceImpl.getClass();

        WSBA wsba = (WSBA) serviceClass.getAnnotation(WSBA.class);
        if (wsba != null)
        {
            CompletionType completionType = wsba.completionType();
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

        WSAT wsat = (WSAT) serviceClass.getAnnotation(WSAT.class);
        if (wsat != null)
        {
            return new WSATHandler(serviceImpl, serviceMethod);
        }

        RESTAT restat = (RESTAT) serviceClass.getAnnotation(RESTAT.class);
        if (restat != null)
        {
            return new RESTATHandler(serviceImpl, serviceMethod);
        }
        
        throw new UnsupportedProtocolException("Expected to find a transaction type annotation on '" + serviceClass.getName() + "'");
    }
}
