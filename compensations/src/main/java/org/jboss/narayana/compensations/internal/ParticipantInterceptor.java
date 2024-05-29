/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

import java.lang.reflect.Method;

/**
 * @author paul.robinson@redhat.com 25/04/2013
 */
public abstract class ParticipantInterceptor {
    private static final Logger LOGGER = Logger.getLogger(ParticipantInterceptor.class);

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        if (!BAControllerFactory.getInstance().isBARunning()) {
            return ic.proceed();
        }

        ParticipantManager participantManager = enlistParticipant(ic.getMethod());

        Object result;
        try {


            result = ic.proceed();
            participantManager.completed();

        } catch (RuntimeException e) {
            LOGGER.warn(e.getMessage(), e);
            participantManager.exit();
            throw e;
        } catch (Exception e) {
            participantManager.completed();
            throw e;
        }
        return result;
    }

    protected abstract ParticipantManager enlistParticipant(Method method) throws Exception;

}