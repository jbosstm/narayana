package org.jboss.jbossts.txframework.impl;

import org.jboss.jbossts.txframework.api.annotation.service.ServiceRequest;
import org.jboss.jbossts.txframework.impl.handlers.HandlerFactory;
import org.jboss.jbossts.txframework.impl.handlers.ProtocolHandler;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

@ServiceRequest
@Interceptor
public class ServiceRequestInterceptor
{
    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Throwable
    {
        Method serviceMethod = ic.getMethod();
        Object serviceImpl = ic.getTarget();

        ProtocolHandler protocolHandler = HandlerFactory.createInstance(serviceImpl, serviceMethod);

        return protocolHandler.proceed(ic);
    }

}
