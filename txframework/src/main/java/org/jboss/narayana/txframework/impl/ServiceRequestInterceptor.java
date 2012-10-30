package org.jboss.narayana.txframework.impl;

import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.impl.handlers.HandlerFactory;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.beans.BeanInfo;
import java.beans.Introspector;
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
        Class serviceClass = getServiceClass(serviceImpl);

        ProtocolHandler protocolHandler = HandlerFactory.createInstance(new ServiceInvocationMeta(serviceImpl, serviceClass, serviceMethod));

        Object result;
        try {
            result = protocolHandler.proceed(ic);
            protocolHandler.notifySuccess();
        } catch (Exception e) {
            protocolHandler.notifyFailure();
            throw e;
        }

        return result;
    }

    private Class getServiceClass(Object serviceImpl) throws Throwable
    {
        if (serviceImpl instanceof TargetInstanceProxy) //Weld proxy
        {
            final BeanInfo bi2 = Introspector.getBeanInfo(serviceImpl.getClass().getSuperclass());
            return bi2.getBeanDescriptor().getBeanClass();
        }
        else //EJB Proxy
        {
            return serviceImpl.getClass();
        }
    }
}
