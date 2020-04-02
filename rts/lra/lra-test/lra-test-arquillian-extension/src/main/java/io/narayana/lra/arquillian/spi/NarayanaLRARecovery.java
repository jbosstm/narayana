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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;

public class NarayanaLRARecovery implements LRARecoveryService {

    @Override
    public void waitForCallbacks(URI lraId) {
        // no action needed
    }

    @Override
    public boolean waitForEndPhaseReplay(URI lraId) {
        String host = lraId.getHost();
        int port = lraId.getPort();
        if (!recoverLRAs(host, port, lraId)) {
            // first recovery scan probably collided with periodic recovery which started
            // before the test execution so try once more
            return recoverLRAs(host, port, lraId);
        }

        return true;
    }

    /**
     * Invokes LRA coordinator recovery REST endpoint and returns whether the recovery of intended LRAs happended
     *
     * @param host  the LRA coordinator host address
     * @param port  the LRA coordinator port
     * @param lraId the LRA id of the LRA that is intended to be recovered
     * @return true the intended LRA recovered, false otherwise
     */
    private boolean recoverLRAs(String host, int port, URI lraId) {
        // trigger a recovery scan
        Client recoveryCoordinatorClient = ClientBuilder.newClient();

        try {
            String recoveryCoordinatorUrl = String.format("http://%s:%d/%s/recovery",
                host, port, LRAConstants.RECOVERY_COORDINATOR_PATH_NAME);
            WebTarget recoveryTarget = recoveryCoordinatorClient.target(URI.create(recoveryCoordinatorUrl));

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
