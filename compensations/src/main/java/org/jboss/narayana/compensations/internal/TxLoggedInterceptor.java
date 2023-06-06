/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.api.TxLogged;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@TxLogged
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 198)
public class TxLoggedInterceptor extends ParticipantInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        return super.intercept(ic);
    }

    @Override
    protected ParticipantManager enlistParticipant(Method method) throws Exception {

        Class<? extends TransactionLoggedHandler> transactionLogHandler = getTransactionLoggedHandler(method);
        return BAControllerFactory.getInstance().enlist(null, null, transactionLogHandler);
    }

    private Class<? extends TransactionLoggedHandler> getTransactionLoggedHandler(Method method) {

        Annotation[] annotations = method.getAnnotations();
        for (Annotation a : annotations) {
            if (a instanceof TxLogged) {
                return ((TxLogged) a).value();
            }
        }
        return null;
    }

}