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

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wst.SystemException;
import org.jboss.narayana.txframework.api.annotation.transaction.Compensatable;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBAHandler;
import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class HandlerFactory {

    private static final Map<String, ProtocolHandler> protocolHandlerMap = new HashMap<String, ProtocolHandler>();

    public static ProtocolHandler getInstance(ServiceInvocationMeta serviceInvocationMeta) throws TXFrameworkException {

        String txid = getCurrentTXID() + ":" + serviceInvocationMeta.getServiceClass();
        ProtocolHandler protocolHandler = protocolHandlerMap.get(txid);

        if (protocolHandler != null) {
            return protocolHandler;
        }

        Compensatable compensatable = (Compensatable) serviceInvocationMeta.getServiceClass().getAnnotation(Compensatable.class);
        if (compensatable != null) {
            protocolHandler = new WSBAHandler(serviceInvocationMeta, compensatable.completionType());
        }

        if (protocolHandler == null) {
            throw new UnsupportedProtocolException("Expected to find a transaction type annotation on '" + serviceInvocationMeta.getServiceClass().getName() + "'");
        }

        protocolHandlerMap.put(txid, protocolHandler);
        return protocolHandler;
    }

    private static boolean isWSATTransactionRunning() {

        UserTransaction ut = UserTransactionFactory.userTransaction();
        return !ut.transactionIdentifier().equals("Unknown");
    }

    private static String getCurrentTXID() throws TXFrameworkException {

        String txid;

        //Try WS-AT
        txid = UserTransactionFactory.userTransaction().transactionIdentifier();
        if (!txid.equals("Unknown")) {
            return txid;
        }

        //Try WS-BA
        try {
            BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager();

            if (businessActivityManager.currentTransaction() != null) {
                txid = businessActivityManager.currentTransaction().toString();
                if (!txid.equals("Unknown")) {
                    return txid;
                }
            }
        } catch (SystemException e) {
            throw new TXFrameworkException("Error when looking up Business Activity", e);
        }

        //Try REST-AT
        HttpServletRequest req = ResteasyProviderFactoryImpl.getInstance().getContextData(HttpServletRequest.class);
        String enlistUrl = req.getHeader("enlistURL");
        if (enlistUrl != null) {
            String[] parts = enlistUrl.split("/");
            return parts[parts.length - 1];
        }

        throw new TXFrameworkException("No Transaction detected");
    }

}
