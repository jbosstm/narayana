package org.jboss.narayana.rest.bridge.inbound.test.common;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

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
