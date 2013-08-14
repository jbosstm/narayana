package org.jboss.narayana.rest.bridge.inbound.test.common;

import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path(ResourceWithTransactionalAnnotation.URL_SEGMENT)
public class ResourceWithTransactionalAnnotation {

    public static final String URL_SEGMENT = "resource-with-transactional-annotation";

    public static final String MANDATORY_SEGMENT = "transactional-mandatory";

    public static final String NEVER_SEGMENT = "transactional-never";

    @POST
    @Path(MANDATORY_SEGMENT)
    @Transactional(Transactional.TxType.MANDATORY)
    public Response transactionalMandatory() {
        return Response.ok().build();
    }

    @POST
    @Path(NEVER_SEGMENT)
    @Transactional(Transactional.TxType.NEVER)
    public Response transactionalNever() throws SystemException {
        return Response.ok().build();
    }

}
