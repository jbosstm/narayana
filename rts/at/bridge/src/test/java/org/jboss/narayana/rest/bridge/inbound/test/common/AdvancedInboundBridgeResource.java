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
package org.jboss.narayana.rest.bridge.inbound.test.common;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


/**
 * REST resource which uses XAResource.
 *
 * @author Gytis Trikleris
 *
 */
@Transactional
@Path(AdvancedInboundBridgeResource.URL_SEGMENT)
public class AdvancedInboundBridgeResource {

    public static final String URL_SEGMENT = "transactional-resource";

    private static LoggingXAResource loggingXAResource;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getInvocations() {
        if (loggingXAResource == null) {
            throw new WebApplicationException(409);
        }

        return Json.createArrayBuilder(loggingXAResource.getInvocations()).build();
    }

    @POST
    public Response enlistXAResource() {
        try {
            loggingXAResource = new LoggingXAResource();

            Transaction t = getTransactionManager().getTransaction();
            t.enlistResource(loggingXAResource);

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError().build();
        }

        return Response.ok().build();
    }

    @PUT
    public Response resetXAResource() {
        if (loggingXAResource != null) {
            loggingXAResource.resetInvocations();
        }

        return Response.ok().build();
    }

    /**
     * Trying to find transaction manager in JNDI. If there is not found
     * then returning Narayana implementation.
     */
    private TransactionManager getTransactionManager() {
        try {
            TransactionManager tm = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
            if (tm == null) {
                tm = (TransactionManager) new InitialContext().lookup("java:jboss/TransactionManager");
            }
            if (tm != null) {
                return tm;
            }
        } catch (NamingException ne) {
            // exception => not able to get transaction manager from container
        }
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }
}
