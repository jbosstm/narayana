/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator has cancelled and completed the protocol.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Cancelled.java,v 1.1 2003/01/07 10:33:45 nmcl Exp $
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
	return "org.w3c.wscf.twophase.status.Cancelled";
    }

    private Cancelled ()
    {
    }

    private static final Cancelled _instance = new Cancelled();
    
}