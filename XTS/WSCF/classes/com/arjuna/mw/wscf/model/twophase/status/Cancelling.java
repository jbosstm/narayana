/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator is in the process of cancelling.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Cancelling.java,v 1.1 2003/01/07 10:33:46 nmcl Exp $
 * @since 1.0.
 */

public class Cancelling implements Status
{

    public static Cancelling instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.twophase.status.Cancelling";
    }

    private Cancelling ()
    {
    }

    private static final Cancelling _instance = new Cancelling();
    
}