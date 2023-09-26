/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The coordinator is preparing.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Compensating.java,v 1.2 2004/03/15 13:25:06 nmcl Exp $
 * @since 1.0.
 */

public class Compensating implements Status
{

    public static Compensating instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "org.w3c.wscf.sagas.status.Compensating";
    }

    private Compensating ()
    {
    }

    private static final Compensating _instance = new Compensating();
    
}