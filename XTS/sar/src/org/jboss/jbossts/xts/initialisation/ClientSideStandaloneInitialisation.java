package org.jboss.jbossts.xts.initialisation;

import com.arjuna.mw.wst11.deploy.WSTXInitialisation;

/**
 * A class used to perform all 1.1 client side initialisation
 */
public class ClientSideStandaloneInitialisation implements XTSInitialisation
{
    public void startup() throws Exception
    {
        // there is no WS-C client startup

        // there is no WS-T client startup for the standalone client

        // there is no WSCF client startup

        // run WSTX startup code

        WSTXInitialisation.startup();
    }

    public void shutdown() throws Exception
    {
        // run WSTX shutdown code

        WSTXInitialisation.shutdown();

        // there is no WSCF client shutdown

        // there is no WS-T client shutdown for the standalone client

        // there is no WS-C coordinator shutdown
    }
}
