/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator is in the process of confirming the participants. This
 * does not necessarily mean that the final outcome will be for the
 * coordination protocol to have confirmed.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Closing.java,v 1.2 2004/03/15 13:25:06 nmcl Exp $
 * @since 1.0.
 */

public class Closing implements Status
{

    public static Closing instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.sagas.status.Closing";
    }

    private Closing ()
    {
    }

    private static final Closing _instance = new Closing();
    
}