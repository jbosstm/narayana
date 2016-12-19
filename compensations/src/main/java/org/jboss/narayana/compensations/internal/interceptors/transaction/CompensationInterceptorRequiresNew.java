/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.compensations.internal.interceptors.transaction;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationTransactionType;
import org.jboss.narayana.compensations.internal.BAController;
import org.jboss.narayana.compensations.internal.BAControllerFactory;
import org.jboss.narayana.compensations.internal.CompensationManagerImpl;
import org.jboss.narayana.compensations.internal.CompensationManagerState;
import org.jboss.narayana.compensations.internal.CurrentTransaction;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Compensatable(CompensationTransactionType.REQUIRES_NEW)
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 197)
public class CompensationInterceptorRequiresNew extends CompensationInterceptorBase {

    /**
     * Request must be invoked in a newly created transaction.
     *
     * If there is an active transaction available, it must be suspended and new transaction created.
     *
     * After request processing, the suspended transaction must be resumed.
     *
     * @param ic
     * @return
     * @throws Exception if request has failed.
     */
    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception {

        BAController baController = BAControllerFactory.getInstance();
        if (!baController.isBARunning()) {
            return invokeInOurTx(ic);
        } else {
            final CurrentTransaction txContext = baController.suspend();
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
