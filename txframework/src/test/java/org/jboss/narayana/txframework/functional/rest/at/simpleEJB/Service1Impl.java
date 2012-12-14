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
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author paul.robinson@redhat.com 06/04/2012
 */
@Stateless
@Transactional
public class Service1Impl implements Service1 {

    @Inject
    Map TXDataMap;

    private boolean rollback = false;
    private EventLog eventLog = new EventLog();

    @WebMethod
    @ServiceRequest
    public Response someServiceRequest(String serviceCommand) throws SomeApplicationException {

        TXDataMap.put("data", "data");

        if (Service1.THROW_APPLICATION_EXCEPTION.equals(serviceCommand)) {
            throw new SomeApplicationException("Intentionally thrown Exception");
        }

        if (VOTE_ROLLBACK.equals(serviceCommand)) {
            rollback = true;
        }
        return Response.ok().build();
    }

    public Response getEventLog() {

        return Response.ok(EventLog.asString(eventLog.getEventLog())).build();
    }

    public Response clearLogs() {

        eventLog.clear();
        return Response.ok().build();
    }

    @Commit
    private void commit() {

        logEvent(Commit.class);
    }

    @Rollback
    private void rollback() {

        logEvent(Rollback.class);
    }

    @Prepare
    private Boolean prepare() {

        logEvent(Prepare.class);
        if (rollback) {
            return false;
        } else {
            return true;
        }
    }

    private void logEvent(Class<? extends Annotation> event) {
        //Check data is available
        if (TXDataMap.get("data") == null) {
            eventLog.addDataUnavailable(event);
        }

        eventLog.addEvent(event);
    }


}
