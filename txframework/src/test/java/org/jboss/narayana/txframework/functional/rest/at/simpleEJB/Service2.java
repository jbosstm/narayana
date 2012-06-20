package org.jboss.narayana.txframework.functional.rest.at.simpleEJB;

import org.jboss.narayana.txframework.functional.common.SomeApplicationException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @Author paul.robinson@redhat.com 06/04/2012
 */
@Path("/2")
public interface Service2 {

    public static final String THROW_APPLICATION_EXCEPTION = "THROW_APPLICATION_EXCEPTION";
    public static final String READ_ONLY="READ_ONLY";
    public static final String VOTE_ROLLBACK="VOTE_ROLLBACK";
    public static final String VOTE_COMMIT="VOTE_COMMIT";

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
