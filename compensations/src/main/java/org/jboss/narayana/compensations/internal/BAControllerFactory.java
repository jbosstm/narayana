package org.jboss.narayana.compensations.internal;

import com.arjuna.mw.wscf.protocols.ProtocolRegistry;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.local.LocalBAController;
import org.jboss.narayana.compensations.internal.remote.RemoteBAController;

/**
 * Factory class to create instances of a {@link BAController} interface.
 *
 * @author paul.robinson@redhat.com 19/04/2014
 */
public class BAControllerFactory {

    /**
     * Get an instance of {@link BAController}.
     * 
     * @return an instance of a remote {@link BAController} implementation is returned if remote compensating transaction is
     *         active. Otherwise local instance of a {@link BAController} is returned.
     */
    public static BAController getInstance() {

        BAController remoteBAController = getRemoteInstance();
        if (remoteBAController.isBARunning()) {
            return remoteBAController;
        }
        BAController localBAController = getLocalInstance();
        if (localBAController.isBARunning()) {
            return localBAController;
        }
        return localBAController;
    }

    /**
     * Get {@link BAController} capable to control remote compensating transactions.
     *
     * @return an instance of a remote {@link BAController}.
     */
    public static BAController getRemoteInstance() {

        return new RemoteBAController(CompensationContextStateManager.getInstance());
    }

    /**
     * Get {@link BAController} capable to control only local compensating transactions.
     *
     * @return an instance of a local {@link BAController}.
     */
    public static BAController getLocalInstance() {

        ProtocolRegistry.sharedManager().initialise();
        return new LocalBAController(CompensationContextStateManager.getInstance());
    }

    /**
     * Check if there is an active local compensating transaction associated with the thread.
     *
     * @return {@code true} if local compensation transaction is running and {@code false} otherwise.
     */
    public static boolean isLocalTransactionRunning() {

        if (getRemoteInstance().isBARunning()) {
            return false;
        }
        BAController localBAController = getLocalInstance();
        return localBAController.isBARunning();
    }

    /**
     * Check if there is an active remote compensating transaction associated with the thread.
     *
     * @return {@code true} if remote compensation transaction is running and {@code false} otherwise.
     */
    public static boolean isRemoteTransactionRunning() {

        return getRemoteInstance().isBARunning();
    }

}
