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
        // no action is needed, tck callbacks calls are sufficiently fast for TCK
    }

    @Override
    public boolean waitForEndPhaseReplay(URI lraId) {
        String host = lraId.getHost();
        int port = lraId.getPort();
        if (!recoverLRAs(host, port, lraId)) {
            // first recovery scan probably collided with periodic recovevery which started
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
