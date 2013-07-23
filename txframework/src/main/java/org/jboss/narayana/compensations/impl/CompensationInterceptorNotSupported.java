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
package org.jboss.narayana.compensations.impl;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationManager;
import org.jboss.narayana.compensations.api.CompensationTransactionType;
import org.jboss.narayana.txframework.impl.TXDataMapImpl;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.Map;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Compensatable(CompensationTransactionType.NOT_SUPPORTED)
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 197)
public class CompensationInterceptorNotSupported extends CompensationInterceptorBase {

    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception {
        if (BusinessActivityManagerFactory.businessActivityManager().currentTransaction() == null) {
            return invokeInNoTx(ic);
        } else {
            final TxContext txContext = BusinessActivityManagerFactory.businessActivityManager().suspend();
            final CompensationManagerState compensationManagerState = CompensationManagerImpl.suspend();
            final Map txDataMap = TXDataMapImpl.getState();
            TXDataMapImpl.suspend();

            try {
                return invokeInNoTx(ic);
            } finally {
                BusinessActivityManagerFactory.businessActivityManager().resume(txContext);
                CompensationManagerImpl.resume(compensationManagerState);
                TXDataMapImpl.resume(txDataMap);
            }
        }
    }

}
