package quickstart;

import org.jboss.jbossts.star.util.TxSupport;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.HttpURLConnection;

/**
 * An example of how a REST resource can act as a participant in a RESTful transaction.
 * For a complete implementation of a participant please refer to the test suite, in particular the inner class:
 * org.jboss.jbossts.star.test.BaseTest$TransactionalResource implements all the responsibilities of a participant
 *
 * The example sends a service request which is handled by the method someServiceRequest. The request includes the
 * URL for registering durable participants within the transaction. This naive implementation assumes every request
 * with a valid enlistment URL is a request a new unit of transactional work and enlists a new URL into the transaction.
 * Thus if a client makes two http requests to the method someServiceRequest then the participant is enlisted twice
 * into the transaction but with different completion URLs.
 */
@Path("service")
public class TransactionAwareResource {
    public static final String PSEGMENT = "service";
    private static int workId = 0;

    @GET
    public String someServiceRequest(@Context UriInfo info, @QueryParam("enlistURL") @DefaultValue("")String enlistUrl) {
        if (enlistUrl == null || enlistUrl.length() == 0)
            return ("non transactional request");

        String serviceURL = info.getBaseUri() + info.getPath();
        String workURL = serviceURL + "/1/" + ++workId;

        String terminator = workURL + "/terminate";
        String participant = workURL + "/terminator";

        String pUrls = TxSupport.getParticipantUrls(terminator, participant);
        System.out.println("enlisting " + pUrls);

        return new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_CREATED}, enlistUrl,
                "POST", TxSupport.POST_MEDIA_TYPE, pUrls, null);
    }

    /*
     * this method handles PUT requests to the url that the participant gave to the REST Atomic Transactions implementation
     * (in the someServiceRequest method). This is the endpoint that the transaction manager interacts with when it needs
     * participants to prepare/commit/rollback their transactional work.
     */
    @PUT
    @Path("{pId}/{wId}/terminate")
    public Response terminate(@PathParam("pId") @DefaultValue("")String pId, @PathParam("wId") @DefaultValue("")String wId, String content) {
        System.out.println("participant terminate resource: PUT request: wId=" + wId + ", status:=" + content);
        String status = TxSupport.getStatus(content);

        if (TxSupport.isPrepare(status)) {
            return Response.ok(TxSupport.toStatusContent(TxSupport.PREPARED)).build();
        } else if (TxSupport.isCommit(status)) {
            return Response.ok(TxSupport.toStatusContent(TxSupport.COMMITTED)).build();
        } else if (TxSupport.isAbort(status)) {
            return Response.ok(TxSupport.toStatusContent(TxSupport.ABORTED)).build();
        } else {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
    }
}
