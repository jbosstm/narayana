/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.jbossts.xts.initialisation;

import com.arjuna.mw.wst11.deploy.WSTXInitialisation;
import com.arjuna.webservices11.wsarjtx.server.TerminationParticipantInitialisation;
import com.arjuna.webservices11.wsat.server.CompletionInitiatorInitialisation;

/**
 * A class used to perform all 1.1 client side initialisation
 */
public class ClientSideInitialisation implements XTSInitialisation
{
    public void startup() throws Exception
    {
        // there is no WS-C client startup

        // run WS-T initialisation code

        CompletionInitiatorInitialisation.startup();
        TerminationParticipantInitialisation.startup();

        // there is no WSCF client startup

        // run WSTX startup code

        WSTXInitialisation.startup();
    }

    public void shutdown() throws Exception
    {
        // run WSTX shutdown code

        WSTXInitialisation.shutdown();

        // there is no WSCF client shutdown

        // run WS-T shutdown code

        TerminationParticipantInitialisation.shutdown();
        CompletionInitiatorInitialisation.startup();

        // there is no WS-C client shutdown
    }
}
