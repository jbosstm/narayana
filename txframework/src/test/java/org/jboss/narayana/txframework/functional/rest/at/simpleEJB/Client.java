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

package org.jboss.narayana.txframework.functional.rest.at.simpleEJB;

import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.impl.handlers.restat.client.Required;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;

/**
 * @author paul.robinson@redhat.com 09/04/2012
 */
public class Client {

    // construct the endpoint for the example web service that will take part in a transaction
    private static final int SERVICE_PORT = 8080;
    private static final String SERVICE_URL = "http://localhost:" + SERVICE_PORT + "/test";

    @Required
    public void invoke() throws Exception {

        Service1 service1 = ProxyFactory.create(Service1.class, SERVICE_URL);
        Service2 service2 = ProxyFactory.create(Service2.class, SERVICE_URL);

        ClientResponse response = (ClientResponse) service1.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

        response = (ClientResponse) service2.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

    }

    public String getEventLog() {

        Service1 service1 = ProxyFactory.create(Service1.class, SERVICE_URL);
        ClientResponse response = (ClientResponse<EventLog>) service1.getEventLog();
        return (String) response.getEntity(String.class);
    }

    public void clearLogs() {

        Service1 service1 = ProxyFactory.create(Service1.class, SERVICE_URL);
        ClientResponse response = (ClientResponse) service1.clearLogs();
        response.releaseConnection();
    }
}
