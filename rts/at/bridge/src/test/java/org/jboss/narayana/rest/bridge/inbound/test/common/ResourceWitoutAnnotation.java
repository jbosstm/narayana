package org.jboss.narayana.rest.bridge.inbound.test.common;

import com.arjuna.ats.jta.UserTransaction;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path(ResourceWitoutAnnotation.URL_SEGMENT)
public class ResourceWitoutAnnotation {

    public static final String URL_SEGMENT = "resource-without-annotation";

    @POST
    public Response noTransaction() throws SystemException {
        if (UserTransaction.userTransaction().getStatus() == Status.STATUS_ACTIVE) {
            return Response.status(412).build();
        }

        return Response.ok().build();
    }

}
