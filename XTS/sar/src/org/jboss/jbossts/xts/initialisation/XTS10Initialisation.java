package org.jboss.jbossts.xts.initialisation;

import com.arjuna.services.framework.startup.Sequencer;
import org.jboss.jbossts.xts.recovery.coordinator.CoordinatorRecoveryInitialisation;
import org.jboss.jbossts.xts.recovery.participant.ParticipantRecoveryInitialisation;

/**
 * A class used to perform all 1.0 initialisation
 */
public class XTS10Initialisation implements XTSInitialisation
{
    public void startup() throws Exception
    {
        // the XTS 1.0 code still uses listeners so we have to alow the sequence of callbacks to execute by
        // opening the latch.

        Sequencer.unlatch();

        // we also need to ensure that recovery modules are started

        CoordinatorRecoveryInitialisation.startup();

        ParticipantRecoveryInitialisation.startup();
    }

    public void shutdown() throws Exception
    {
        // we just need to ensure that recovery modules are shut down

        ParticipantRecoveryInitialisation.startup();

        CoordinatorRecoveryInitialisation.startup();
    }
}
