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
import org.jboss.narayana.txframework.impl.Participant;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

/**
 * @author paul.robinson@redhat.com, 2012-04-10
 */
public class RESTATHandler implements ProtocolHandler {

    private Participant participant;

    public RESTATHandler(ServiceInvocationMeta serviceInvocationMeta) throws TXFrameworkException {

        HttpServletRequest req = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        String enlistUrl = req.getHeader("enlistURL");
        UriInfo info = ResteasyProviderFactory.getContextData(UriInfo.class);
        String txid = getTransactionId(enlistUrl);

        participant = RestParticipantEndpointImpl.enlistParticipant(txid, info, enlistUrl, serviceInvocationMeta);
    }

    @Override
    public Object proceed(InvocationContext ic) throws Exception {

        participant.resume();
        return ic.proceed();
    }

    @Override
    public void notifySuccess() {

        Participant.suspend();
    }

    @Override
    public void notifyFailure() {
        //Todo: ensure transaction rolled back
        Participant.suspend();
    }

    private static String getTransactionId(String enlistUrl) {

        String[] parts = enlistUrl.split("/");
        return parts[parts.length - 1];
    }

}
