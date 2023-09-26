/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator has cancelled and completed the protocol.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Cancelled.java,v 1.2 2004/03/15 13:25:05 nmcl Exp $
 * @since 1.0.
 */

public class Cancelled implements Status
{

    public static Cancelled instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.sagas.status.Cancelled";
    }

    private Cancelled ()
    {
    }

    private static final Cancelled _instance = new Cancelled();
    
}