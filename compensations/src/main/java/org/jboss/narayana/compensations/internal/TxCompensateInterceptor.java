/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.TxCompensate;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@TxCompensate
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 198)
public class TxCompensateInterceptor extends ParticipantInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        return super.intercept(ic);
    }

    @Override
    protected ParticipantManager enlistParticipant(Method method) throws Exception {

        Class<? extends CompensationHandler> compensationHandler = getCompensationHandler(method);
        return BAControllerFactory.getInstance().enlist(compensationHandler, null, null);
    }

    private Class<? extends CompensationHandler> getCompensationHandler(Method method) {

        Annotation[] annotations = method.getAnnotations();
        for (Annotation a : annotations) {
            if (a instanceof TxCompensate) {
                return ((TxCompensate) a).value();
            }
        }
        return null;
    }

}