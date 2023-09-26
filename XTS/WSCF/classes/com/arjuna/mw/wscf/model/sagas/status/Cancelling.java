/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator is in the process of cancelling.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Cancelling.java,v 1.2 2004/03/15 13:25:05 nmcl Exp $
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
	return "org.w3c.wscf.sagas.status.Cancelling";
    }

    private Cancelling ()
    {
    }

    private static final Cancelling _instance = new Cancelling();
    
}