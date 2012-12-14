/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.impl.handlers;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import org.jboss.narayana.txframework.api.annotation.transaction.Compensatable;
import org.jboss.narayana.txframework.api.annotation.transaction.Transactional;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.restat.service.RESTATHandler;
import org.jboss.narayana.txframework.impl.handlers.wsat.WSATHandler;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBACoordinatorCompletionHandler;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBAParticipantCompletionHandler;

public class HandlerFactory {

    //todo: improve the way transaction type is detected.
    public static ProtocolHandler createInstance(ServiceInvocationMeta serviceInvocationMeta) throws TXFrameworkException {

        Compensatable Compensatable = (Compensatable) serviceInvocationMeta.getServiceClass().getAnnotation(Compensatable.class);
        if (Compensatable != null) {
            CompletionType completionType = Compensatable.completionType();
            if (completionType == CompletionType.PARTICIPANT) {
                return new WSBAParticipantCompletionHandler(serviceInvocationMeta);
            } else if (completionType == CompletionType.COORDINATOR) {
                return new WSBACoordinatorCompletionHandler(serviceInvocationMeta);
            } else {
                throw new UnsupportedProtocolException("Unexpected or null completionType");
            }
        }

        Transactional Transactional = (Transactional) serviceInvocationMeta.getServiceClass().getAnnotation(Transactional.class);
        if (Transactional != null) {
            if (isWSATTransactionRunning()) {
                return new WSATHandler(serviceInvocationMeta);
            } else //assume it must be a REST-AT transaction running.
            {
                return new RESTATHandler(serviceInvocationMeta);
            }
        }
        throw new UnsupportedProtocolException("Expected to find a transaction type annotation on '" + serviceInvocationMeta.getServiceClass().getName() + "'");
    }

    private static boolean isWSATTransactionRunning() {

        UserTransaction ut = UserTransactionFactory.userTransaction();
        return !ut.transactionIdentifier().equals("Unknown");
    }

}
