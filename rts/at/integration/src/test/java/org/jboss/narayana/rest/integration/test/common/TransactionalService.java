package org.jboss.narayana.rest.integration.test.common;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.narayana.rest.integration.ParticipantInformation;
import org.jboss.narayana.rest.integration.ParticipantsContainer;
import org.jboss.narayana.rest.integration.api.Aborted;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.ReadOnly;
import org.jboss.narayana.rest.integration.api.Vote;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@Path("/" + TransactionalService.PATH_SEGMENT)
public final class TransactionalService {

    public static final String APPLICATION_ID = "org.jboss.narayana.rest.integration.test.common.TransactionalService";

    public static final String PATH_SEGMENT = "transactional-service";

    @GET
    public Response getParticipantInvocations(@QueryParam("participantId") final String participantId) {
        ParticipantInformation information = ParticipantsContainer.getInstance().getParticipantInformation(participantId);

        if (information == null) {
            return Response.status(404).build();
        }

        final JsonArray jsonArray = Json.createArrayBuilder(((LoggingParticipant) information.getParticipant()).getInvocations()).build();

        return Response.ok().entity(jsonArray).build();
    }

    @POST
    public String enlistParticipant(@QueryParam("participantEnlistmentUrl") final String participantEnlistmentUrl,
                                    @QueryParam("vote") final String stringVote, @Context final UriInfo uriInfo) {

        final Vote vote = stringVoteToVote(stringVote);
        final LoggingParticipant participant = new LoggingParticipant(vote);

        return ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, participantEnlistmentUrl, participant).toString();
    }

    @PUT
    public void registerDeserializer() {
        ParticipantsManagerFactory.getInstance().registerDeserializer(APPLICATION_ID, new TestParticipantDeserializer());
    }

    private Vote stringVoteToVote(final String stringVote) {
        if (Prepared.class.getName().equals(stringVote)) {
            return new Prepared();
        } else if (ReadOnly.class.getName().equals(stringVote)) {
            return new ReadOnly();
        }

        return new Aborted();
    }

}
