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

import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Transactional;
import org.jboss.narayana.txframework.api.management.TXDataMap;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author paul.robinson@redhat.com 06/04/2012
 */
@Stateless
@Transactional
@Path("/1")
public class Service1Impl {

    public static final String THROW_APPLICATION_EXCEPTION = "THROW_APPLICATION_EXCEPTION";
    public static final String READ_ONLY = "READ_ONLY";
    public static final String VOTE_ROLLBACK = "VOTE_ROLLBACK";
    public static final String VOTE_COMMIT = "VOTE_COMMIT";

    @Inject
    private TXDataMap<String, String> txDataMap;

    private EventLog eventLog = new EventLog();

    @WebMethod
    @ServiceRequest
    @POST
    @Produces("text/plain")
    @Consumes("text/plain")
    public Response someServiceRequest(String serviceCommand) throws SomeApplicationException {

        txDataMap.put("data", "data");

        if (Service1.THROW_APPLICATION_EXCEPTION.equals(serviceCommand)) {
            throw new SomeApplicationException("Intentionally thrown Exception");
        }

        if (VOTE_ROLLBACK.equals(serviceCommand)) {
            txDataMap.put("rollback", "true");
        }
        return Response.ok().build();
    }
    @GET
    @Path("getEventLog")
    public Response getEventLog() {

        return Response.ok(EventLog.asString(eventLog.getEventLog())).build();
    }
    @GET
    @Path("clearLogs")
    public Response clearLogs() {

        eventLog.clear();
        return Response.ok().build();
    }

    @Commit
    public void commit() {

        logEvent(Commit.class);
    }

    @Rollback
    public void rollback() {

        logEvent(Rollback.class);
    }

    @Prepare
    public Boolean prepare() {

        logEvent(Prepare.class);
        if (txDataMap.containsKey("rollback")) {
            return false;
        } else {
            return true;
        }
    }

    private void logEvent(Class<? extends Annotation> event) {
        //Check data is available
        if (txDataMap.get("data") == null) {
            eventLog.addDataUnavailable(event);
        }

        eventLog.addEvent(event);
    }


}
