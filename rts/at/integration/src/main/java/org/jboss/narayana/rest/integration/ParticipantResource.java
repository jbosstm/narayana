/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.api.Aborted;
import org.jboss.narayana.rest.integration.api.HeuristicException;
import org.jboss.narayana.rest.integration.api.HeuristicType;
import org.jboss.narayana.rest.integration.api.ParticipantException;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.ReadOnly;
import org.jboss.narayana.rest.integration.api.Vote;

import com.arjuna.ats.arjuna.common.arjPropertyManager;


/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@Path(ParticipantResource.BASE_PATH_SEGMENT + "/{participantId}")
public final class ParticipantResource {

    public static final String BASE_PATH_SEGMENT = "rest-at-participant";

    private static final Logger LOG = Logger.getLogger(ParticipantResource.class);

    @HEAD
    public Response getTerminatorUrl(@PathParam("participantId") final String participantId, @Context final UriInfo uriInfo) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("HEAD request on ParticipantResource. ParticipantId: " + participantId);
        }

        if (ParticipantsContainer.getInstance().getParticipantInformation(participantId) == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Participant with id " + participantId + " was not found.");
            }

            return Response.status(404).build();
        }

        Response.ResponseBuilder builder = Response.ok();
        TxSupport.addLinkHeader(builder, uriInfo, TxLinkNames.TERMINATOR, TxLinkNames.TERMINATOR);

        return builder.build();
    }

    @GET
    @Produces(TxMediaType.TX_STATUS_MEDIA_TYPE)
    public Response getStatus(@PathParam("participantId") final String participantId) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("GET request on ParticipantResource. ParticipantId: " + participantId);
        }

        final ParticipantInformation participantInformation = ParticipantsContainer.getInstance()
                .getParticipantInformation(participantId);

        if (participantInformation == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Participant with id " + participantId + " was not found.");
            }

            return Response.status(404).build();
        }

        return Response.ok(TxSupport.toStatusContent(participantInformation.getStatus())).build();
    }

    @PUT
    @Consumes(TxMediaType.TX_STATUS_MEDIA_TYPE)
    @Produces(TxMediaType.TX_STATUS_MEDIA_TYPE)
    public Response terminate(@PathParam("participantId") final String participantId, final String content)
            throws HeuristicException {

        if (LOG.isTraceEnabled()) {
            LOG.trace("PUT request on ParticipantResource. ParticipantId: " + participantId + ", content: " + content);
        }

        final ParticipantInformation participantInformation = ParticipantsContainer.getInstance()
                .getParticipantInformation(participantId);

        if (participantInformation == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Participant with id " + participantId + " was not found.");
            }

            return Response.status(404).build();
        }

        String status = TxSupport.getStatus(content);

        if (TxStatus.isPrepare(status)) {
            if (!canPrepare(participantInformation)) {
                return Response.status(412).build();
            }

            Vote vote = prepare(participantInformation);
            return voteToResponse(vote);

        } else if (TxStatus.isCommit(status)) {
            if (!canCommit(participantInformation)) {
                return Response.status(412).build();
            }

            commit(participantInformation);
            return Response.ok().entity(TxSupport.toStatusContent(TxStatus.TransactionCommitted.name())).build();

        } else if (TxStatus.isCommitOnePhase(status)) {
            if (!canCommitOnePhase(participantInformation)) {
                return Response.status(412).build();
            }

            commitOnePhase(participantInformation);
            return Response.ok().entity(TxSupport.toStatusContent(TxStatus.TransactionCommittedOnePhase.name())).build();

        } else if (TxStatus.isAbort(status)) {
            rollback(participantInformation);
            return Response.ok().entity(TxSupport.toStatusContent(TxStatus.TransactionRolledBack.name())).build();
        }

        return Response.status(400).build();
    }

    @DELETE
    public Response forgetHeuristic(@PathParam("participantId") final String participantId) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("DELETE request on ParticipantResource. ParticipantId: " + participantId);
        }

        final ParticipantInformation participantInformation = ParticipantsContainer.getInstance()
                .getParticipantInformation(participantId);

        if (participantInformation == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Participant with id " + participantId + " was not found.");
            }

            return Response.status(404).build();
        }

        if (TxStatus.TransactionHeuristicCommit.name().equals(participantInformation.getStatus())
                || TxStatus.TransactionHeuristicRollback.name().equals(participantInformation.getStatus())
                || TxStatus.TransactionHeuristicHazard.name().equals(participantInformation.getStatus())
                || TxStatus.TransactionHeuristicMixed.name().equals(participantInformation.getStatus())) {

            RecoveryManager.getInstance().removeParticipantInformation(participantInformation);
            ParticipantsContainer.getInstance().removeParticipantInformation(participantInformation.getId());

            return Response.ok().build();
        }

        return Response.status(412).build();
    }

    private Vote prepare(final ParticipantInformation participantInformation) throws HeuristicException {
        if (isHeuristic(participantInformation)) {
            return prepareHeuristic(participantInformation);
        }

        participantInformation.setStatus(TxStatus.TransactionPreparing.name());

        final Vote vote;
        try {
            vote = participantInformation.getParticipant().prepare();
        } catch (ParticipantException e) {
            if (arjPropertyManager.getCoreEnvironmentBean().isLogAndRethrow()) {
                RESTATLogger.atI18NLogger.warn_prepareParticipantResource(e.getMessage(), e); // JBTM-3990
            }
            participantInformation.setStatus(TxStatus.TransactionActive.name());
            throw e;
        }

        if (vote instanceof Aborted) {
            rollback(participantInformation);
        } else if (vote instanceof Prepared) {
            participantInformation.setStatus(TxStatus.TransactionPrepared.name());
            RecoveryManager.getInstance().persistParticipantInformation(participantInformation);
        } else if (vote instanceof ReadOnly) {
            readOnly(participantInformation);
        }

        return vote;
    }

    private Vote prepareHeuristic(final ParticipantInformation participantInformation) {
        if (TxStatus.TransactionHeuristicCommit.name().equals(participantInformation.getStatus())) {
            return new Prepared();
        } else {
            RecoveryManager.getInstance().removeParticipantInformation(participantInformation);
            return new Aborted();
        }
    }

    private void commit(final ParticipantInformation participantInformation) throws HeuristicException {
        if (isHeuristic(participantInformation)) {
            commitHeuristic(participantInformation);
        } else {
            participantInformation.setStatus(TxStatus.TransactionCommitting.name());

            try {
                participantInformation.getParticipant().commit();
            } catch (HeuristicException e) {
                if (!e.getHeuristicType().equals(HeuristicType.HEURISTIC_COMMIT)) {
                    if (arjPropertyManager.getCoreEnvironmentBean().isLogAndRethrow()) {
                        RESTATLogger.atI18NLogger.warn_heuristicRollbackParticipantResource(e.getMessage(), e); // JBTM-3990
                    }
                    participantInformation.setStatus(e.getHeuristicType().toTxStatus());
                    RecoveryManager.getInstance().persistParticipantInformation(participantInformation);
                    throw new HeuristicException(e.getHeuristicType());
                }
            } catch (ParticipantException e) {
                if (arjPropertyManager.getCoreEnvironmentBean().isLogAndRethrow()) {
                    RESTATLogger.atI18NLogger.warn_participantRollbackParticipantResource(e.getMessage(), e); // JBTM-3990
                }
                participantInformation.setStatus(TxStatus.TransactionPrepared.name());
                throw e;
            }

            participantInformation.setStatus(TxStatus.TransactionCommitted.name());
            RecoveryManager.getInstance().removeParticipantInformation(participantInformation);
            ParticipantsContainer.getInstance().removeParticipantInformation(participantInformation.getId());
        }
    }

    private void commitHeuristic(final ParticipantInformation participantInformation) throws HeuristicException {
        if (!TxStatus.TransactionHeuristicCommit.name().equals(participantInformation.getStatus())) {
            throw new HeuristicException(HeuristicType.fromTxStatus(participantInformation.getStatus()));
        } else {
            participantInformation.setStatus(TxStatus.TransactionCommitted.name());
            RecoveryManager.getInstance().removeParticipantInformation(participantInformation);
            ParticipantsContainer.getInstance().removeParticipantInformation(participantInformation.getId());
        }
    }

    private void commitOnePhase(final ParticipantInformation participantInformation) {
        if (!isHeuristic(participantInformation)) {
            participantInformation.setStatus(TxStatus.TransactionCommitting.name());

            try {
                participantInformation.getParticipant().commitOnePhase();
            } catch (ParticipantException e) {
                participantInformation.setStatus(TxStatus.TransactionActive.name());
                throw e;
            }

            participantInformation.setStatus(TxStatus.TransactionCommittedOnePhase.name());
            ParticipantsContainer.getInstance().removeParticipantInformation(participantInformation.getId());
        }
    }

    private void rollback(final ParticipantInformation participantInformation) throws HeuristicException {
        if (isHeuristic(participantInformation)) {
            rollbackHeuristic(participantInformation);
        } else {
            final String previousStatus = participantInformation.getStatus();
            participantInformation.setStatus(TxStatus.TransactionRollingBack.name());

            try {
                participantInformation.getParticipant().rollback();
            } catch (HeuristicException e) {
                if (!e.getHeuristicType().equals(HeuristicType.HEURISTIC_ROLLBACK)) {
                    participantInformation.setStatus(e.getHeuristicType().toTxStatus());
                    RecoveryManager.getInstance().persistParticipantInformation(participantInformation);
                    throw new HeuristicException(e.getHeuristicType());
                }
            } catch (ParticipantException e) {
                participantInformation.setStatus(previousStatus);
                throw e;
            }

            participantInformation.setStatus(TxStatus.TransactionRolledBack.name());
            RecoveryManager.getInstance().removeParticipantInformation(participantInformation);
            ParticipantsContainer.getInstance().removeParticipantInformation(participantInformation.getId());
        }
    }

    private void rollbackHeuristic(final ParticipantInformation participantInformation) throws HeuristicException {
        if (!TxStatus.TransactionHeuristicRollback.name().equals(participantInformation.getStatus())) {
            throw new HeuristicException(HeuristicType.fromTxStatus(participantInformation.getStatus()));
        } else {
            participantInformation.setStatus(TxStatus.TransactionRolledBack.name());
            RecoveryManager.getInstance().removeParticipantInformation(participantInformation);
            ParticipantsContainer.getInstance().removeParticipantInformation(participantInformation.getId());
        }
    }

    private void readOnly(final ParticipantInformation participantInformation) {
        participantInformation.setStatus(TxStatus.TransactionReadOnly.name());

        try {
            ClientBuilder.newClient().target(participantInformation.getRecoveryURL()).request().delete();
        } catch (Exception e) {
            RESTATLogger.atI18NLogger.warn_readOnlyParticipantResource(e.getMessage(), e);
        }

        ParticipantsContainer.getInstance().removeParticipantInformation(participantInformation.getId());
    }

    private Response voteToResponse(final Vote vote) {
        Response response;

        if (vote instanceof Prepared) {
            response = Response.ok().entity(TxSupport.toStatusContent(TxStatus.TransactionPrepared.name())).build();
        } else if (vote instanceof ReadOnly) {
            response = Response.ok().entity(TxSupport.toStatusContent(TxStatus.TransactionReadOnly.name())).build();
        } else {
            response = Response.status(409).entity(TxSupport.toStatusContent(TxStatus.TransactionRolledBack.name())).build();
        }

        return response;
    }

    private boolean canPrepare(final ParticipantInformation participantInformation) {
        return TxStatus.TransactionActive.name().equals(participantInformation.getStatus())
                || isHeuristic(participantInformation);
    }

    private boolean canCommit(final ParticipantInformation participantInformation) {
        return TxStatus.TransactionPrepared.name().equals(participantInformation.getStatus())
                || isHeuristic(participantInformation);
    }

    private boolean canCommitOnePhase(final ParticipantInformation participantInformation) {
        return TxStatus.TransactionActive.name().equals(participantInformation.getStatus())
                || isHeuristic(participantInformation);
    }

    private boolean isHeuristic(final ParticipantInformation participantInformation) {
        return TxStatus.TransactionHeuristicCommit.name().equals(participantInformation.getStatus())
                || TxStatus.TransactionHeuristicRollback.name().equals(participantInformation.getStatus())
                || TxStatus.TransactionHeuristicMixed.name().equals(participantInformation.getStatus())
                || TxStatus.TransactionHeuristicHazard.name().equals(participantInformation.getStatus());
    }

}
