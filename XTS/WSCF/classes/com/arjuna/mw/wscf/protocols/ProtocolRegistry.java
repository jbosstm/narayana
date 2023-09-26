/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.protocols;

import com.arjuna.mw.wscf.protocols.ProtocolManager;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ProtocolRegistry.java,v 1.2 2003/03/04 12:55:56 nmcl Exp $
 * @since 1.0.
 */

// TODO we need a separate instance for WSTX

public class ProtocolRegistry
{

    public static ProtocolManager sharedManager ()
    {
	return _shared;
    }

    public static ProtocolManager createManager ()
    {
	return new ProtocolManager();
    }

    private static ProtocolManager _shared = new ProtocolManager();

}