/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wsc11.deploy;

import com.arjuna.mw.wscf.protocols.ProtocolRegistry;

/**
 * Initialise WSCF.
 * @author kevin
 */
public class WSCFInitialisation
{
    public static void startup()
    {
        // ensure we load any required protocol implementations

        ProtocolRegistry.sharedManager().initialise();
    }

    public static void shutdown()
    {
    }
}