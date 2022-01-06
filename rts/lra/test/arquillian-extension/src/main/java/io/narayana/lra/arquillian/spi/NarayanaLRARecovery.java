/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
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

package io.narayana.lra.arquillian.spi;

import io.narayana.lra.LRAConstants;
import org.eclipse.microprofile.lra.tck.service.spi.LRARecoveryService;
import org.jboss.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import static io.narayana.lra.LRAConstants.RECOVERY_COORDINATOR_PATH_NAME;

public class NarayanaLRARecovery implements LRARecoveryService {
    private static final Logger log = Logger.getLogger(NarayanaLRARecovery.class);

    /*
     * A bit of hacking to change the internals of annotations defined in LRA TCK.
     * There is need to adjust timeout defined on the annotation definition.
     */
    static {
        String[] resourceClassNames = new String[]{
                "org.eclipse.microprofile.lra.tck.participant.api.LraResource",
                "org.eclipse.microprofile.lra.tck.participant.api.RecoveryResource"};
        for (String resourceClassName: resourceClassNames) {
            try {
                Class<?> clazz = Class.forName(resourceClassName);
                LRAAnnotationAdjuster.processWithClass(clazz);
            } catch (ClassNotFoundException e) {
                log.debugf("Cannot load class %s to adjust LRA annotation on the class", resourceClassName);
            }
        }
    }

    @Override
    public void waitForCallbacks(URI lraId) {
        // no action needed
    }

    @Override
    public boolean waitForEndPhaseReplay(URI lraId) {
        if (!recoverLRAs(lraId)) {
            // first recovery scan probably collided with periodic recovery which started
            // before the test execution so try once more
            return recoverLRAs(lraId);
        }
        return true;
    }

    /**
     * Invokes LRA coordinator recovery REST endpoint and returns whether the recovery of intended LRAs happened
     *
     * @param lraId the LRA id of the LRA that is intended to be recovered
     * @return true the intended LRA recovered, false otherwise
     */
    private boolean recoverLRAs(URI lraId) {
        // trigger a recovery scan
        Client recoveryCoordinatorClient = ClientBuilder.newClient();

        try {
            URI lraCoordinatorUri = LRAConstants.getLRACoordinatorUrl(lraId);
            URI recoveryCoordinatorUri = UriBuilder.fromUri(lraCoordinatorUri)
                    .path(RECOVERY_COORDINATOR_PATH_NAME).build();
            WebTarget recoveryTarget = recoveryCoordinatorClient.target(recoveryCoordinatorUri);

            // send the request to the recovery coordinator
            Response response = recoveryTarget.request().get();
            String json = response.readEntity(String.class);
            response.close();

            if (json.contains(lraId.toASCIIString())) {
                // intended LRA didn't recover
                return false;
            }

            return true;
        } finally {
            recoveryCoordinatorClient.close();
        }
    }
}
