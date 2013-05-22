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
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.TransactionRolledBackException;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@Compensatable
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 197)
public class CompensationInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
        BusinessActivityManager bam = BusinessActivityManagerFactory.businessActivityManager();

        TxContext currentTX = bam.currentTransaction();

        boolean begun = false;
        if (currentTX == null) {
            uba.begin();
            begun = true;
            CompensationManagerImpl.resume(new CompensationManagerState());
        }


        Object result;
        try {

            result = ic.proceed();

        } catch (RuntimeException e) {
            if (begun) {
                uba.cancel();
            }
            throw e;
        }

        if (begun) {

            if (!CompensationManagerImpl.isCompensateOnly()) {
                try {
                    uba.close();
                } catch (TransactionRolledBackException e) {
                    throw new TransactionCompensatedException("Failed to close transaction", e);
                }
            } else {
                uba.cancel();
                throw new TransactionCompensatedException("Transaction was marked as 'compensate only'");
            }
            CompensationManagerImpl.suspend();
        }
        return result;
    }

}
