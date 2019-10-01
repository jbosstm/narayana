package io.narayana.lra.arquillian.spi;

import io.narayana.lra.LRAConstants;
import io.narayana.lra.client.NarayanaLRAClient;
import org.eclipse.microprofile.lra.tck.service.spi.LraRecoveryService;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;

@ApplicationScoped
public class NarayanaLRARecovery implements LraRecoveryService {

    @Override
    public void triggerRecovery() {
        recoverLRAs(
            System.getProperty(NarayanaLRAClient.LRA_COORDINATOR_HOST_KEY, "localhost"),
            Integer.parseInt(System.getProperty(NarayanaLRAClient.LRA_COORDINATOR_PORT_KEY, "8080")));
    }

    @Override
    public void triggerRecovery(URI lraId) {
        String host = lraId.getHost();
        int port = lraId.getPort();
        if (!recoverLRAs(host, port, lraId)) {
            // first recovery scan probably collided with periodic recovevery which started
            // before the test execution so try once more
            recoverLRAs(host, port, lraId);
        }
    }

    /**
     * Invokes LRA coordinator recovery REST endpoint and returns whether the recovery of intended LRAs happended
     *
     * @param host the LRA coordinator host address
     * @param port the LRA coordinator port
     * @param lraIds the LRA ids of the LRAs that are intended to be recovered
     * @return true all intended LRAs recovered, false otherwise
     */
    private boolean recoverLRAs(String host, int port, URI... lraIds) {
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

            for (URI lraId : lraIds) {
                if (json.contains(lraId.toASCIIString())) {
                    // intended LRA didn't recover
                    return false;
                }
            }

            return true;
        } finally {
            recoveryCoordinatorClient.close();
        }

    }
}
