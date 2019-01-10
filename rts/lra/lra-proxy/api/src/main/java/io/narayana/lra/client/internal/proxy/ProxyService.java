/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.lra.client.internal.proxy;

import org.eclipse.microprofile.lra.participant.LRAParticipant;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.participant.JoinLRAException;
import org.eclipse.microprofile.lra.participant.LRAManagement;
import org.eclipse.microprofile.lra.participant.TerminationException;
import io.narayana.lra.proxy.logging.LRAProxyLogger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

import static io.narayana.lra.client.internal.proxy.ParticipantProxyResource.LRA_PROXY_PATH;
import static org.eclipse.microprofile.lra.client.LRAClient.LRA_COORDINATOR_HOST_KEY;
import static org.eclipse.microprofile.lra.client.LRAClient.LRA_COORDINATOR_PATH_KEY;
import static org.eclipse.microprofile.lra.client.LRAClient.LRA_COORDINATOR_PORT_KEY;

@ApplicationScoped
public class ProxyService implements LRAManagement {
    private static final String TIMELIMIT_PARAM_NAME = "TimeLimit";  // LRAClient.TIMELIMIT_PARAM_NAME

    private static List<ParticipantProxy> participants; // TODO figure out why ProxyService is constructed twice

    private Client lcClient;
    private WebTarget lcTarget;

    private UriBuilder uriBuilder;

    @PostConstruct
    void init() {
        if (participants == null) { // TODO figure out why ProxyService is constructed twice
            participants = new ArrayList<>();
        }

        int httpPort = Integer.getInteger("swarm.http.port", 8081);
        String httpHost = System.getProperty("swarm.http.host", "localhost");

        // TODO if the proxy is restarted on a different endpoint it should notify the recovery coordinator

        uriBuilder = UriBuilder.fromPath(LRA_PROXY_PATH + "/{lra}/{pid}");
        uriBuilder.scheme("http")
                .host(httpHost)
                .port(httpPort);

        String lcHost = System.getProperty(LRA_COORDINATOR_HOST_KEY, "localhost");
        int lcPort = Integer.getInteger(LRA_COORDINATOR_PORT_KEY, 8080);
        String lcPath = System.getProperty(LRA_COORDINATOR_PATH_KEY, "lra-coordinator");


        UriBuilder urib = UriBuilder.fromPath(lcPath).scheme("http").host(lcHost).port(lcPort);

        lcClient = ClientBuilder.newClient();
        lcTarget = lcClient.target(urib.build());
    }

    @PreDestroy
    void fini() {
        if (lcClient != null) {
            lcClient.close();
            lcClient = null;
        }
    }

    private ParticipantProxy getProxy(URL lraId, String participantId) {
        int i = participants.indexOf(new ParticipantProxy(lraId, participantId));

        return (i == -1 ? null : participants.get(i));
    }

    private ParticipantProxy recreateProxy(URL lraId, String participantId) {
        return new ParticipantProxy(lraId, participantId);
    }

    Response notifyParticipant(URL lraId, String participantId, String participantData, boolean compensate) {
        ParticipantProxy proxy = getProxy(lraId, participantId);

        if (proxy == null) {
            /*
             * must be in a recovery scenario so recreate the proxy from the registered data
             */
            proxy = recreateProxy(lraId, participantId);
        }

        LRAParticipant participant = proxy.getParticipant();

        if (participant == null && participantData != null && participantData.length() > 0) {
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
            } catch (TerminationException e) {
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
            LRAProxyLogger.logger.errorf("TODO recovery: null participant for callback %s", lraId.toExternalForm());
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    void notifyForget(URL lraId, String participantId) {
        ParticipantProxy proxy = getProxy(lraId, participantId);

        if (proxy != null) {
            participants.remove(proxy);
        }
    }

    ParticipantStatus getStatus(URL lraId, String participantId) throws InvalidLRAStateException {
        ParticipantProxy proxy = getProxy(lraId, participantId);

        if (proxy == null) {
            throw new NotFoundException();
        }

        Optional<ParticipantStatus> status = proxy.getStatus();

        // null status implies that the participant is still active
        return status.orElseThrow(InvalidLRAStateException::new);
    }

    @Override
    public URL joinLRA(LRAParticipant participant, URL lraId)
            throws JoinLRAException {
        return joinLRA(participant, lraId, 0L, ChronoUnit.SECONDS);
    }

    @Override
    public URL joinLRA(LRAParticipant participant, URL lraId, Long timelimit, ChronoUnit unit)
            throws JoinLRAException {
        // TODO if lraId == null then register a join all nw LRAs
        ParticipantProxy proxy = new ParticipantProxy(lraId, UUID.randomUUID().toString(), participant);

        try {
            String pId = proxy.getParticipantId();
            String lra = URLEncoder.encode(lraId.toExternalForm(), "UTF-8");
            UriBuilder clone = uriBuilder.clone();

            URI participantUri = clone.build(lra, pId);
            Link link = Link.fromUri(participantUri)
                    .rel("participant").type("text/plain")
                    .build();

            Optional<String> participantData = serializeParticipant(participant);

            participants.add(proxy);

            Response response = lcTarget.path(lra)
                    .queryParam(TIMELIMIT_PARAM_NAME, timelimit)
                    .request()
                    .header("Link", link.toString())
                    .put(Entity.entity(participantData.orElse(""), MediaType.TEXT_PLAIN));

            if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                throw new JoinLRAException(lraId, response.getStatus(), "Too late to join with this LRA", null);
            } else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new JoinLRAException(lraId, response.getStatus(), "Unable to join with this LRA", null);
            }

            return new URL(response.readEntity(String.class));
        } catch (Exception e) {
            throw new JoinLRAException(lraId, 0, "Exception whilst joining with this LRA", e);
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

    private static Optional<LRAParticipant> deserializeParticipant(URL lraId, final String objectAsString) {
        return Optional.empty(); // TODO
/*        final byte[] data = Base64.getDecoder().decode(objectAsString);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return Optional.of((LRAParticipant) ois.readObject());
        } catch (final IOException | ClassNotFoundException e) {
            for (LRAParticipantDeserializer ds : deserializers) {
                LRAParticipant participant = ds.deserialize(lraId, data);

                if (participant != null) {
                    return Optional.of(participant);
                }
            }

            LRAProxyLogger.i18NLogger.warn_cannotDeserializeParticipant(lraId.toExternalForm(),
                    deserializers.size() == 0 ? "null" : deserializers.get(0).getClass().getCanonicalName(),
                    e.getMessage());

            return Optional.empty();
        }*/
    }
}
