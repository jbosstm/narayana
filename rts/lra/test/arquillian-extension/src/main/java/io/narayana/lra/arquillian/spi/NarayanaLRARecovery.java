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

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
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
    private static final long WAIT_CALLBACK_TIMEOUT = initWaitForCallbackTimeout();
    private static final String WAIT_CALLBACK_TIMEOUT_PROPERTY = "lra.tck.callback.timeout";

    /*
     * Wait for the participant to return the callback. This method does not
     * guarantee callbacks to finish. The Participant status is not immediately
     * reflected to the LRA status, but only after a recovery scan which executes an
     * enlistment. The waiting time can be configurable by
     * LRAConstants.WAIT_CALLBACK_TIMEOUT property.
     */
    @Override
    public void waitForCallbacks(URI lraId) {
        log.trace("waitForCallbacks for: " + lraId.toASCIIString());
        try {
            Thread.sleep(WAIT_CALLBACK_TIMEOUT);
        }
        catch (InterruptedException e) {
            log.error("waitForCallbacks interrupted by " + e.getMessage());
        }
    }

    @Override
    public boolean waitForEndPhaseReplay(URI lraId) {
        log.trace("waitForEndPhaseReplay for: " + lraId.toASCIIString());
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

    private static Integer initWaitForCallbackTimeout() {
        Config config = ConfigProvider.getConfig();
        int defaultValue = 1000;
        if (config != null) {
            try {
                return config.getOptionalValue(WAIT_CALLBACK_TIMEOUT_PROPERTY, Integer.class).orElse(defaultValue);
            }
            catch (IllegalArgumentException e) {
                log.error("property " + WAIT_CALLBACK_TIMEOUT_PROPERTY + " not set correctly, using the default value: "
                        + defaultValue);
            }
        }
        return defaultValue;
    }
}
