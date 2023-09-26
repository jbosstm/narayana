/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator has confirmed and completed the protocol.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Confirmed.java,v 1.1 2003/01/07 10:33:46 nmcl Exp $
 * @since 1.0.
 */

public class Confirmed implements Status
{

    public static Confirmed instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.twophase.status.Confirmed";
    }

    private Confirmed ()
    {
    }

    private static final Confirmed _instance = new Confirmed();
    
}