/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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