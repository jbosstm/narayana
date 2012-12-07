package org.jboss.jbossts.xts.environment;

import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.util.ArrayList;
import java.util.List;

/**
 * bean storing recovery implementation configuration values derived from the xts properties file,
 * system property settings and, in case we are running inside JBossAS the xts bean.xml file
 */
@PropertyPrefix(prefix = "org.jboss.jbossts.xts.recovery.")
public class RecoveryEnvironmentBean
{
    /**
     * the list of XTS recovery modules to be installed at startup and removed at shutdown
     */
    @ConcatenationPrefix(prefix="org.jboss.jbossts.xts.recovery.coordinatorRecoveryModule")
    private volatile List<String> coordinatorRecoveryModules = new ArrayList<String>();

    /**
     * the list of XTS recovery modules to be installed at startup and removed at shutdown
     */
    @ConcatenationPrefix(prefix="org.jboss.jbossts.xts.recovery.participantRecoveryModule")
    private volatile List<String> participantRecoveryModules = new ArrayList<String>();

    /**
     * Returns the list of XTS coordinator recovery modules to be installed at startup and removed at shutdown.
     *
     * @return the list of XTS coordinator recovery modules.
     */
    public List<String> getCoordinatorRecoveryModules() {
        return coordinatorRecoveryModules;
    }

    /**
     * Sets the list of XTS coordinator recovery modules to be installed at startup and removed at shutdown.
     *
     * @param coordinatorRecoveryModules the list of XTS coordinator recovery modules.
     */
    public void setCoordinatorRecoveryModules(List<String> coordinatorRecoveryModules) {
        this.coordinatorRecoveryModules = coordinatorRecoveryModules;
    }

    /**
     * Returns the list of XTS participant recovery modules to be installed at startup and removed at shutdown.
     *
     * @return the list of XTS participant recovery modules.
     */
    public List<String> getParticipantRecoveryModules() {
        return participantRecoveryModules;
    }

    /**
     * Sets the list of XTS participant recovery modules to be installed at startup and removed at shutdown.
     *
     * @param participantRecoveryModules the list of XTS participant recovery modules.
     */
    public void setParticipantRecoveryModules(List<String> participantRecoveryModules) {
        this.participantRecoveryModules = participantRecoveryModules;
    }
}
