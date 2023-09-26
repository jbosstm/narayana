/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.bridge.inbound.test.common;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Stateless
@Path(ResourceWithTransactionAttributeAnnotation.URL_SEGMENT)
public class ResourceWithTransactionAttributeAnnotation {

    public static final String URL_SEGMENT = "resource-with-transaction-attribute-annotation";

    public static final String MANDATORY_SEGMENT = "transaction-attribute-mandatory";

    public static final String NEVER_SEGMENT = "transaction-attribute-never";

    @POST
    @Path(MANDATORY_SEGMENT)
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Response transactionAttributeMandatory() {
        return Response.ok().build();
    }

    @POST
    @Path(NEVER_SEGMENT)
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Response transactionAttributeNever() {
        return Response.ok().build();
    }

}
