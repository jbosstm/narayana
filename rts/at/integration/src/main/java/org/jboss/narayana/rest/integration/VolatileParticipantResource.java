package org.jboss.narayana.rest.integration;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.api.VolatileParticipant;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path(VolatileParticipantResource.BASE_PATH_SEGMENT + "/{participantId}")
public class VolatileParticipantResource {

    public static final String BASE_PATH_SEGMENT = "volatile-rest-at-participant";

    private static final Logger LOG = Logger.getLogger(VolatileParticipantResource.class);

    @PUT
    public Response beforeCompletion(@PathParam("participantId") final String participantId) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("beforeCompletion request on VolatileParticipantResource. ParticipantId: " + participantId);
        }

        final VolatileParticipant volatileParticipant =
                ParticipantsContainer.getInstance().getVolatileParticipant(participantId);

        if (volatileParticipant == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Volatile participant with id " + participantId + " was not found.");
            }

            return Response.status(404).build();
        }

        if (beforeCompletion(volatileParticipant)) {
            return Response.ok().build();
        }

        return Response.status(409).build();
    }

    @PUT
    @Consumes(TxMediaType.TX_STATUS_MEDIA_TYPE)
    public Response afterCompletion(@PathParam("participantId") final String participantId, final String content) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("afterCompletion request on VolatileParticipantResource. ParticipantId: " + participantId
                    + ", content: " + content);
        }

        final VolatileParticipant volatileParticipant =
                ParticipantsContainer.getInstance().getVolatileParticipant(participantId);

        if (volatileParticipant == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Volatile participant with id " + participantId + " was not found.");
            }

            return Response.status(404).build();
        }

        if (afterCompletion(participantId, volatileParticipant, content)) {
            return Response.ok().build();
        }

        return Response.status(409).build();
    }

    private boolean beforeCompletion(final VolatileParticipant volatileParticipant) {
        try {
            volatileParticipant.beforeCompletion();
        } catch (Throwable t) {
            RESTATLogger.atI18NLogger.warn_beforeVolatileParticipantResource(t.getMessage(), t);
            return false;
        }

        return true;
    }

    private boolean afterCompletion(final String participantId, final VolatileParticipant volatileParticipant,
            final String content) {

        final TxStatus txStatus = TxSupport.toTxStatus(content);

        try {
            volatileParticipant.afterCompletion(txStatus);
        } catch (Throwable t) {
            RESTATLogger.atI18NLogger.warn_afterVolatileParticipantResource(t.getMessage(), t);
            return false;
        } finally {
            ParticipantsContainer.getInstance().removeVolatileParticipant(participantId);
        }

        return true;
    }

}