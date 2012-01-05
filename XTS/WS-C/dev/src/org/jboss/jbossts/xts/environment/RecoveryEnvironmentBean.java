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

    public List<String> getCoordinatorRecoveryModules() {
        return coordinatorRecoveryModules;
    }

    public void setCoordinatorRecoveryModules(List<String> coordinatorRecoveryModules) {
        this.coordinatorRecoveryModules = coordinatorRecoveryModules;
    }

    public List<String> getParticipantRecoveryModules() {
        return participantRecoveryModules;
    }

    public void setParticipantRecoveryModules(List<String> participantRecoveryModules) {
        this.participantRecoveryModules = participantRecoveryModules;
    }
}
