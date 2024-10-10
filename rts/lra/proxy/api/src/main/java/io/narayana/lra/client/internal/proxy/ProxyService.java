/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.client.internal.proxy;

import io.narayana.lra.client.internal.NarayanaLRAClient;
import io.narayana.lra.proxy.logging.LRAProxyLogger;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

import static io.narayana.lra.client.internal.proxy.ParticipantProxyResource.LRA_PROXY_PATH;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@ApplicationScoped
public class ProxyService {
    private static List<ParticipantProxy> participants;

    @Inject
    private NarayanaLRAClient narayanaLRAClient;

    private UriBuilder uriBuilder;

    @PostConstruct
    void init() {
        if (participants == null) {
            participants = new ArrayList<>();
        }

        int httpPort = Integer.getInteger("lra.http.port", 8081);
        String httpHost = System.getProperty("lra.http.host", "localhost");

        // Note that if the proxy is restarted on a different endpoint it should notify the recovery coordinator

        uriBuilder = UriBuilder.fromPath(LRA_PROXY_PATH + "/{lra}/{pid}");
        uriBuilder.scheme("http")
                .host(httpHost)
                .port(httpPort);
    }

    private ParticipantProxy getProxy(URI lraId, String participantId) {
        int i = participants.indexOf(new ParticipantProxy(lraId, participantId));

        return (i == -1 ? null : participants.get(i));
    }

    private ParticipantProxy recreateProxy(URI lraId, String participantId) {
        return new ParticipantProxy(lraId, participantId);
    }

    Response notifyParticipant(URI lraId, String participantId, String participantData, boolean compensate) {
        ParticipantProxy proxy = getProxy(lraId, participantId);

        if (proxy == null) {
            /*
             * must be in a recovery scenario so recreate the proxy from the registered data
             */
            proxy = recreateProxy(lraId, participantId);
        }

        LRAProxyParticipant participant = proxy.getParticipant();

        if (participant == null && participantData != null && !participantData.isEmpty()) {
            participant = deserializeParticipant(lraId, participantData).orElse(null);
        }

        if (participant != null) {
            Future<Void> future = null;

            try {
                if (compensate) {
                    // let any NotFoundException propagate back to the coordinator
                    future = participant.compensateWork(lraId);
                } else {
                    // let any NotFoundException propagate back to the coordinator
                    future = participant.completeWork(lraId);
                }
            } catch (Exception e) {
                return Response.ok().entity(compensate ? ParticipantStatus.FailedToCompensate
                        : ParticipantStatus.FailedToComplete).build();
            } finally {
                if (future == null) {
                    participants.remove(proxy); // we definitively know the outcome
                } else {
                    proxy.setFuture(future, compensate); // remember the future so that we can report its progress
                }
            }

            if (future != null) {
                return Response.accepted().build();
            }

            return Response.ok().build();
        } else {
            LRAProxyLogger.logger.errorf("proxy recovery: null participant for callback %s", lraId.toASCIIString());
        }

        return Response.status(NOT_FOUND).build();
    }

    void notifyForget(URI lraId, String participantId) {
        ParticipantProxy proxy = getProxy(lraId, participantId);

        if (proxy != null) {
            participants.remove(proxy);
        }
    }

    ParticipantStatus getStatus(URI lraId, String participantId) throws InvalidLRAStateException {
        ParticipantProxy proxy = getProxy(lraId, participantId);

        if (proxy == null) {
            String errorMsg = String.format("Cannot find participant proxy for LRA id %s, participant id %s",
                    lraId, participantId);
            throw new NotFoundException(errorMsg,
                    Response.status(NOT_FOUND).entity(errorMsg).build());
        }

        Optional<ParticipantStatus> status = proxy.getStatus();

        // null status implies that the participant is still active
        return status.orElseThrow(InvalidLRAStateException::new);
    }

    public URI joinLRA(LRAProxyParticipant participant, URI lraId) {
        return joinLRA(participant, lraId, 0L, ChronoUnit.SECONDS);
    }

    public URI joinLRA(LRAProxyParticipant participant, URI lraId, Long timeLimit, ChronoUnit unit) {
        ParticipantProxy proxy = new ParticipantProxy(lraId, UUID.randomUUID().toString(), participant);

        try {
            String pId = proxy.getParticipantId();
            String lra = URLEncoder.encode(lraId.toASCIIString(), StandardCharsets.UTF_8);

            participants.add(proxy);

            Optional<String> compensatorData = serializeParticipant(participant);
            URI participantUri = uriBuilder.build(lra, pId);
            long timeLimitInSeconds = Duration.of(timeLimit, unit).getSeconds();
            StringBuilder sb = new StringBuilder(compensatorData.orElse(""));

            return narayanaLRAClient.joinLRA(lraId, timeLimitInSeconds, participantUri, sb);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.status(0)
                .entity(lraId + ": Exception whilst joining with this LRA").build());
        }
    }

    private static Optional<String> serializeParticipant(final Serializable object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);

            return Optional.of(Base64.getEncoder().encodeToString(baos.toByteArray()));
        } catch (final IOException e) {
            LRAProxyLogger.i18NLogger.error_cannotSerializeParticipant(e.toString(), e);

            return Optional.empty();
        }
    }

    private static Optional<LRAProxyParticipant> deserializeParticipant(URI lraId, final String objectAsString) {
        return Optional.empty();
    }
}
