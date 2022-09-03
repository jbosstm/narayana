package org.jboss.narayana.rest.bridge.inbound;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.TransactionRequiredException;
import jakarta.transaction.TransactionalException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Provider
public class TransactionalExceptionMapper implements ExceptionMapper<TransactionalException> {
    public static final String TRANSACTIONA_REQUIRED_MESSAGE = "REST-AT transaction is required for this request.";

    public static final String INVALID_TRANSACTIONA_MESSAGE = "REST-AT transaction is not supported by this resource.";


    @Override
    public Response toResponse(TransactionalException exception) {
        if (exception.getCause() instanceof InvalidTransactionException) {
            return Response.status(412).entity(INVALID_TRANSACTIONA_MESSAGE).build();
        } else if (exception.getCause() instanceof TransactionRequiredException) {
            return Response.status(412).entity(TRANSACTIONA_REQUIRED_MESSAGE).build();
        }

        throw exception;
    }
}