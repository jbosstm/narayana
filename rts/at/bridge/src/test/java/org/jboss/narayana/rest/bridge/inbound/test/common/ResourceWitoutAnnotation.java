package org.jboss.narayana.rest.bridge.inbound.test.common;

import com.arjuna.ats.jta.UserTransaction;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

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
