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

package org.jboss.narayana.compensations.internal.interceptors.participant;

import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TxConfirm;
import org.jboss.narayana.compensations.internal.BAControllerFactory;
import org.jboss.narayana.compensations.internal.ParticipantManager;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@TxConfirm
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 198)
public class TxConfirmInterceptor extends ParticipantInterceptor {

    /**
     * This request has a confirmation handler attached which has to be enlisted to the transaction.
     *
     * @param ic
     * @return
     * @throws Exception
     */
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
