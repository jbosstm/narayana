/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator is in the process of confirming the participants. This
 * does not necessarily mean that the final outcome will be for the
 * coordination protocol to have confirmed.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Confirming.java,v 1.1 2003/01/07 10:33:46 nmcl Exp $
 * @since 1.0.
 */

public class Confirming implements Status
{

    public static Confirming instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.twophase.status.Confirming";
    }

    private Confirming ()
    {
    }

    private static final Confirming _instance = new Confirming();
    
}