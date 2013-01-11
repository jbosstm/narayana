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

import org.jboss.narayana.txframework.functional.SomeApplicationException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * @author paul.robinson@redhat.com 06/04/2012
 */
@Path("/1")
public interface Service1 {

    public static final String THROW_APPLICATION_EXCEPTION = "THROW_APPLICATION_EXCEPTION";
    public static final String READ_ONLY = "READ_ONLY";
    public static final String VOTE_ROLLBACK = "VOTE_ROLLBACK";
    public static final String VOTE_COMMIT = "VOTE_COMMIT";

    @POST
    @Produces("text/plain")
    @Consumes("text/plain")
    public Response someServiceRequest(String serviceCommand) throws SomeApplicationException;

    @GET
    @Path("getEventLog")
    public Response getEventLog();

    @GET
    @Path("clearLogs")
    public Response clearLogs();

}
