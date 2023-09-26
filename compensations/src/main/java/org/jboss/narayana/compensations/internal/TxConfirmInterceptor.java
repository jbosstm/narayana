/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TxConfirm;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@TxConfirm
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 198)
public class TxConfirmInterceptor extends ParticipantInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        return super.intercept(ic);
    }

    @Override
    protected ParticipantManager enlistParticipant(Method method) throws Exception {

        Class<? extends ConfirmationHandler> confirmationHandler = getConfirmationHandler(method);
        return BAControllerFactory.getInstance().enlist(null, confirmationHandler, null);
    }

    private Class<? extends ConfirmationHandler> getConfirmationHandler(Method method) {

        Annotation[] annotations = method.getAnnotations();
        for (Annotation a : annotations) {
            if (a instanceof TxConfirm) {
                return ((TxConfirm) a).value();
            }
        }
        return null;
    }
}