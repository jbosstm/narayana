/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.internal;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationTransactionType;
import org.jboss.narayana.compensations.api.TransactionRequiredException;
import org.jboss.narayana.compensations.api.TransactionalException;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Compensatable(CompensationTransactionType.MANDATORY)
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 197)
public class CompensationInterceptorMandatory extends CompensationInterceptorBase {

    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception {

        BAController baController = BAControllerFactory.getInstance();
        if (!baController.isBARunning()) {
            throw new TransactionalException("Transaction is required for invocation", new TransactionRequiredException());
        }

        return invokeInCallerTx(ic);
    }

}