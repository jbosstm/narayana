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

package org.jboss.narayana.txframework.impl.handlers.restat.client;

import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

import javax.ws.rs.ext.Provider;

/**
 * @author paul.robinson@redhat.com 08/04/2012
 */
@ClientInterceptor
@Provider
public class RestClientInterceptor implements ClientExecutionInterceptor {

    public ClientResponse execute(ClientExecutionContext clientExecutionContext) throws Exception {

        //Add getDurableParticipantEnlistmentURI if a REST-TX is running
        TxSupport txSupport = UserTransaction.getTXSupport();
        if (txSupport != null) {
            String enlistURL = txSupport.getDurableParticipantEnlistmentURI();
            clientExecutionContext.getRequest().header("enlistURL", enlistURL);
        }

        return clientExecutionContext.proceed();
    }
}
