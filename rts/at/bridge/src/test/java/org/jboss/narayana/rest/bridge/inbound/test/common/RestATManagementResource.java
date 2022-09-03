/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.rest.bridge.inbound.test.common;

import org.jboss.narayana.rest.integration.ParticipantInformation;
import org.jboss.narayana.rest.integration.ParticipantsContainer;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Map;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path(RestATManagementResource.BASE_URL_SEGMENT)
public class RestATManagementResource {

    public static final String BASE_URL_SEGMENT = "rest-at-management-resource";

    public static final String PARTICIPANTS_URL_SEGMENT = "participants";

    @GET
    @Path(PARTICIPANTS_URL_SEGMENT)
    public String getAllParticipantsInformation() {
        final Map<String, ParticipantInformation> participantsInformation =
                ParticipantsContainer.getInstance().getAllParticipantsInformation();

        return participantsInformationToJSON(participantsInformation).toString();
    }

    private JsonArray participantsInformationToJSON(final Map<String, ParticipantInformation> participantsInformation) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (final ParticipantInformation participantInformation : participantsInformation.values()) {
            final JsonObject value = participantInformationToJSON(participantInformation);
            if (value != null) {
                arrayBuilder.add(value);
            }
        }

        return arrayBuilder.build();
    }

    private JsonObject participantInformationToJSON(final ParticipantInformation participantInformation) {
        if (participantInformation == null) {
            return null;
        }

        return Json.createObjectBuilder()
                .add("id", participantInformation.getId())
                .add("applicationId", participantInformation.getApplicationId())
                .add("recoveryURL", participantInformation.getRecoveryURL())
                .add("status", participantInformation.getStatus())
                .build();
    }

}
