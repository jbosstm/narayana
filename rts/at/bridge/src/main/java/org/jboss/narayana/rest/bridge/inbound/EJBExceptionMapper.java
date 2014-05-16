package org.jboss.narayana.rest.bridge.inbound;

import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRequiredException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {

    public static final String TRANSACTIONA_REQUIRED_MESSAGE = "REST-AT transaction is required for this request.";

    public static final String INVALID_TRANSACTIONA_MESSAGE = "REST-AT transaction is not supported by this resource.";

    @Override
    public Response toResponse(EJBException exception) {
        if (exception instanceof EJBTransactionRequiredException) {
            return Response.status(412).entity(TRANSACTIONA_REQUIRED_MESSAGE).build();
        } else if (exception.getMessage().contains("WFLYEJB0063")) {
            return Response.status(412).entity(INVALID_TRANSACTIONA_MESSAGE).build();
        }

        throw exception;
    }

}
