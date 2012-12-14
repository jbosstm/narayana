/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
public class ServiceRequestInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Throwable {

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

    private Class getServiceClass(Object serviceImpl) throws Throwable {

        if (serviceImpl instanceof TargetInstanceProxy) //Weld proxy
        {
            final BeanInfo bi2 = Introspector.getBeanInfo(serviceImpl.getClass().getSuperclass());
            return bi2.getBeanDescriptor().getBeanClass();
        } else //EJB Proxy
        {
            return serviceImpl.getClass();
        }
    }
}
