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

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import org.jboss.narayana.txframework.impl.TXDataMapImpl;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author paul.robinson@redhat.com 25/04/2013
 */
public abstract class ParticipantInterceptor {


    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {


        BusinessActivityManager bam = BusinessActivityManagerFactory.businessActivityManager();

        boolean initilisedDataMap = false;
        if (!TXDataMapImpl.isActive()) {
            TXDataMapImpl.resume(new HashMap());
            initilisedDataMap = true;
        }

        BAParticipantManager participantManager = enlistParticipant(bam, ic.getMethod());


        Object result;
        try {


            result = ic.proceed();
            participantManager.completed();

        } catch (RuntimeException e) {
            participantManager.exit();
            throw e;
        } catch (Exception e) {
            participantManager.completed();
            throw e;
        } finally {
            if (initilisedDataMap) {
                TXDataMapImpl.suspend();
            }
        }

        return result;
    }

    protected abstract BAParticipantManager enlistParticipant(BusinessActivityManager bam, Method method) throws WrongStateException, UnknownTransactionException, SystemException;

}
