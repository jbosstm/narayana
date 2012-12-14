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

package org.jboss.narayana.txframework.impl.handlers.restat.service;

import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;
import org.jboss.narayana.txframework.impl.handlers.wsat.WSATParticipantRegistry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

/**
 * @author paul.robinson@redhat.com, 2012-04-10
 */
public class RESTATHandler implements ProtocolHandler {

    private ServiceInvocationMeta serviceInvocationMeta;

    private static final WSATParticipantRegistry participantRegistry = new WSATParticipantRegistry();

    public RESTATHandler(ServiceInvocationMeta serviceInvocationMeta) throws TXFrameworkException {

        this.serviceInvocationMeta = serviceInvocationMeta;

        registerParticipant(serviceInvocationMeta);
    }

    @Override
    public Object proceed(InvocationContext ic) throws Exception {

        return ic.proceed();
    }

    @Override
    public void notifySuccess() {
        //do nothing
    }

    @Override
    public void notifyFailure() {
        //Todo: ensure transaction rolled back
    }

    private void registerParticipant(ServiceInvocationMeta serviceInvocationMeta) throws ParticipantRegistrationException {

        HttpServletRequest req = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        String enlistUrl = req.getHeader("enlistURL");
        UriInfo info = ResteasyProviderFactory.getContextData(UriInfo.class);
        String txid = getTransactionId(enlistUrl);

        synchronized (participantRegistry) {

            //Only create participant if there is not already a participant for this ServiceImpl and this transaction
            if (!participantRegistry.isRegistered(txid, serviceInvocationMeta.getServiceClass())) {
                RestParticipantEndpointImpl.enlistParticipant(txid, info, enlistUrl, serviceInvocationMeta);
                //todo: need to remove this when done.
                participantRegistry.register(txid, serviceInvocationMeta.getServiceClass());
            }
        }
    }

    private static String getTransactionId(String enlistUrl) {

        String[] parts = enlistUrl.split("/");
        return parts[parts.length - 1];
    }

}
