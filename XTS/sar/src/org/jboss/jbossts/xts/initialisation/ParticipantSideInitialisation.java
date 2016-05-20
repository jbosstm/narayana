package org.jboss.jbossts.xts.initialisation;

import com.arjuna.mw.wst11.deploy.WSTXInitialisation;
import com.arjuna.webservices11.wsat.server.ParticipantInitialisation;
import com.arjuna.webservices11.wsba.server.CoordinatorCompletionParticipantInitialisation;
import com.arjuna.webservices11.wsba.server.ParticipantCompletionParticipantInitialisation;
import com.arjuna.wst11.messaging.deploy.WSTParticipantInitialisation;
import org.jboss.jbossts.xts.recovery.participant.ParticipantRecoveryInitialisation;

/**
 * A class used to perform all 1.1 participant side initialisation
 */
public class ParticipantSideInitialisation implements XTSInitialisation
{
    public void startup() throws Exception
    {
        // there is no WS-C coordinator startup

        // run WS-T startup code

        ParticipantInitialisation.startup();

        CoordinatorCompletionParticipantInitialisation.startup();
        ParticipantCompletionParticipantInitialisation.startup();

        WSTParticipantInitialisation.startup();

        // there is no WSCF coordinator startup

        // run WSTX startup code

        WSTXInitialisation.startup();

        // run recovery startup code

        ParticipantRecoveryInitialisation.startup();
    }

    public void shutdown() throws Exception
    {
        // run recovery shutdown code

        ParticipantRecoveryInitialisation.shutdown();

        // run WSTX shutdown code

        WSTXInitialisation.shutdown();

        // there is no WSCF coordinator shutdown
        // there is no WS-C coordinator shutdown

        // run WS-T shutdown code
        WSTParticipantInitialisation.shutdown();

        ParticipantCompletionParticipantInitialisation.shutdown();
        CoordinatorCompletionParticipantInitialisation.shutdown();

        ParticipantInitialisation.shutdown();

        // there is no WS-C coordinator shutdown
    }
}
