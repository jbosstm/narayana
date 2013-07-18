package org.jboss.narayana.rest.integration.test.common;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.ParticipantInformation;
import org.jboss.narayana.rest.integration.ParticipantsContainer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Map;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path(RestATManagementResource.BASE_URL_SEGMENT)
public class RestATManagementResource {

    public static final String BASE_URL_SEGMENT = "rest-at-management-resource";

    public static final String PARTICIPANTS_URL_SEGMENT = "participants";

    private static final Logger LOG = Logger.getLogger(RestATManagementResource.class);

    @GET
    @Path(PARTICIPANTS_URL_SEGMENT)
    public String getAllParticipantsInformation() {
        final Map<String, ParticipantInformation> participantsInformation =
                ParticipantsContainer.getInstance().getAllParticipantsInformation();

        return participantsInformationToJSON(participantsInformation).toString();
    }

    private JSONArray participantsInformationToJSON(final Map<String, ParticipantInformation> participantsInformation) {
        final JSONArray array = new JSONArray();

        for (final ParticipantInformation participantInformation : participantsInformation.values()) {
            final JSONObject value = participantInformationToJSON(participantInformation);
            if (value != null) {
                array.put(value);
            }
        }

        return array;
    }

    private JSONObject participantInformationToJSON(final ParticipantInformation participantInformation) {
        if (participantInformation == null) {
            return null;
        }

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", participantInformation.getId());
            jsonObject.put("applicationId", participantInformation.getApplicationId());
            jsonObject.put("recoveryURL", participantInformation.getRecoveryURL());
            jsonObject.put("status", participantInformation.getStatus());
        } catch (JSONException e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }

        return jsonObject;
    }

}
