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
        doTriggerRecovery(null, System.getProperty(NarayanaLRAClient.LRA_COORDINATOR_HOST_KEY),
            Integer.parseInt(System.getProperty(NarayanaLRAClient.LRA_COORDINATOR_PORT_KEY)));
    }

    @Override
    public void triggerRecovery(URI lraId) {
        doTriggerRecovery(lraId, lraId.getHost(), lraId.getPort());
    }

    private void doTriggerRecovery(URI lraId, String host, int port) {
        if (invokeCoordinatorRecovery(lraId, host, port)) {
            // first recovery scan probably collided with periodic recovevery which started
            // before the test execution so try once more
            invokeCoordinatorRecovery(null, host, port);
        }
    }

    /**
     * Invokes LRA coordinator recovery REST endpoint and returns whether the recovery of intended LRA happended
     *
     * @param lraId LRA id of the LRA that is expected to recover
     * @param host the LRA coordinator host address
     * @param port the LRA coordinator port
     * @return true if the expected LRA recovered and no repeated recovery is necessary, false otherwise
     */
    private boolean invokeCoordinatorRecovery(URI lraId, String host, int port) {
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

            return lraId != null && !json.contains(lraId.toASCIIString());
        } finally {
            recoveryCoordinatorClient.close();
        }

    }
}
