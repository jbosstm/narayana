/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

import org.jboss.narayana.compensations.api.CancelOnFailure;
import org.jboss.narayana.compensations.api.CompensationManager;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * @author paul.robinson@redhat.com 25/04/2013
 */
@CancelOnFailure
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 199)
public class CancelOnFailureInterceptor {

    @Inject
    CompensationManager compensationManager;

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        try {
            return ic.proceed();
        } catch (RuntimeException e) {
            compensationManager.setCompensateOnly();
            throw e;
        }
    }
}