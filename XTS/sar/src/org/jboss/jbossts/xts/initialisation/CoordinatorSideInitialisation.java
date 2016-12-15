package org.jboss.jbossts.xts.initialisation;

import com.arjuna.mw.wsc11.deploy.WSCFInitialisation;
import com.arjuna.webservices11.wsarjtx.server.TerminationCoordinatorInitialisation;
import com.arjuna.webservices11.wsarjtx.server.TerminationCoordinatorRPCInitialisation;
import com.arjuna.webservices11.wsat.server.CompletionCoordinatorInitialisation;
import com.arjuna.webservices11.wsat.server.CompletionCoordinatorRPCInitialisation;
import com.arjuna.webservices11.wsat.server.CoordinatorInitialisation;
import com.arjuna.webservices11.wsba.server.CoordinatorCompletionCoordinatorInitialisation;
import com.arjuna.webservices11.wsba.server.ParticipantCompletionCoordinatorInitialisation;
import com.arjuna.webservices11.wscoor.server.ActivationCoordinatorInitialisation;
import com.arjuna.webservices11.wscoor.server.RegistrationCoordinatorInitialisation;
import com.arjuna.wsc11.messaging.deploy.CoordinationInitialisation;
import com.arjuna.wst11.messaging.deploy.WSTCoordinatorInitialisation;
import org.jboss.jbossts.xts.recovery.coordinator.CoordinatorRecoveryInitialisation;

/**
 * A class used to perform all 1.1 coordinator side initialisation
 */
public class CoordinatorSideInitialisation implements XTSInitialisation
{
    public void startup() throws Exception
    {
        // run WS-C initialisation code

        ActivationCoordinatorInitialisation.startup();
        RegistrationCoordinatorInitialisation.startup();
        CoordinationInitialisation.startup();

        // run WS-T startup code

        CoordinatorInitialisation.startup();
        CompletionCoordinatorInitialisation.startup();
        CompletionCoordinatorRPCInitialisation.startup();

        CoordinatorCompletionCoordinatorInitialisation.startup();
        ParticipantCompletionCoordinatorInitialisation.startup();
        TerminationCoordinatorInitialisation.startup();
        TerminationCoordinatorRPCInitialisation.startup();

        WSTCoordinatorInitialisation.startup();

        // run WSCF startup code
        
        WSCFInitialisation.startup();

        // there is no WSTX coordinator startup

        // run recovery startup code

        CoordinatorRecoveryInitialisation.startup();
    }

    public void shutdown() throws Exception
    {
        // run recovery shutdown code

        CoordinatorRecoveryInitialisation.shutdown();

        // there is no WSTX coordinator shutdown

        // run WSCF shutdown code

        WSCFInitialisation.shutdown();

        // run WS-T shutdown code

        WSTCoordinatorInitialisation.shutdown();

        TerminationCoordinatorRPCInitialisation.shutdown();
        TerminationCoordinatorInitialisation.shutdown();
        ParticipantCompletionCoordinatorInitialisation.shutdown();
        CoordinatorCompletionCoordinatorInitialisation.shutdown();

        CompletionCoordinatorInitialisation.shutdown();
        CoordinatorInitialisation.shutdown();

        // run WS-C shutdown code
        CoordinationInitialisation.shutdown();
        RegistrationCoordinatorInitialisation.shutdown();
        ActivationCoordinatorInitialisation.shutdown();

    }
}
