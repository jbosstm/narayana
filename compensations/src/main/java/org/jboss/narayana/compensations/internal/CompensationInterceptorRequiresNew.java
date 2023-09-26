/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.internal;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationTransactionType;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Compensatable(CompensationTransactionType.REQUIRES_NEW)
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 197)
public class CompensationInterceptorRequiresNew extends CompensationInterceptorBase {

    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception {

        BAController baController = BAControllerFactory.getInstance();
        if (!baController.isBARunning()) {
            return invokeInOurTx(ic);
        } else {
            final Object txContext = baController.suspend();
            final CompensationManagerState compensationManagerState = CompensationManagerImpl.suspend();

            try {
                return invokeInOurTx(ic);
            } finally {
                baController.resume(txContext);
                CompensationManagerImpl.resume(compensationManagerState);
            }
        }
    }

}